package com.coap.core.network.stack;

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.MessageObserverAdapter;
import com.coap.core.coap.Response;
import com.coap.core.coap.CoAP.*;
import com.coap.core.network.Exchange;
import com.coap.core.network.Exchange.*;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.observe.ObserveRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP observe layer.
 */
public class ObserveLayer extends AbstractLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObserveLayer.class.getName());

	/**
	 * Creates a new observe layer for a configuration.
	 * 
	 * @param config The configuration values to use.
	 */
	public ObserveLayer(final NetworkConfig config) {
		// so far no configuration values for this layer
	}

	@Override
	public void sendResponse(final Exchange exchange, final Response response) {

		final ObserveRelation relation = exchange.getRelation();
		if (relation != null && relation.isEstablished()) {

			if (exchange.getRequest().isAcknowledged() || exchange.getRequest().getType() == Type.NON) {
				// Transmit errors as CON
				if (!ResponseCode.isSuccess(response.getCode())) {
					LOGGER.debug("response has error code {} and must be sent as CON", response.getCode());
					response.setType(Type.CON);
					relation.cancel();
				} else {
					// Make sure that every now and than a CON is mixed within
					if (relation.check()) {
						LOGGER.debug("observe relation check requires the notification to be sent as CON");
						response.setType(Type.CON);
					} else {
						// By default use NON, but do not override resource
						// decision
						if (response.getType() == null) {
							response.setType(Type.NON);
						}
					}
				}
			}

			/*
			 * Only one Confirmable message is allowed to be in transit. A CON
			 * is in transit as long as it has not been acknowledged, rejected,
			 * or timed out. All further notifications are postponed here. If a
			 * former CON is acknowledged or timeouts, it starts the freshest
			 * notification (In case of a timeout, it keeps the retransmission
			 * counter). When a fresh/younger notification arrives but must be
			 * postponed we forget any former notification.
			 */
			if (response.getType() == Type.CON) {
				prepareSelfReplacement(exchange, response);
			}

			// The decision whether to postpone this notification or not and the
			// decision which notification is the freshest to send next must be
			// synchronized
			Response current = relation.getCurrentControlNotification();
			if (current != null && isInTransit(current)) {
				LOGGER.debug("a former notification is still in transit. Postponing {}", response);
				relation.setNextControlNotification(response);
				// do not send now
				return;
			} else {
				relation.setCurrentControlNotification(response);
				relation.setNextControlNotification(null);
			}

			/*
			 * The matcher must be able to find the NON notifications to remove
			 * them from the exchangesByMID hashmap
			 */
			if (response.getType() == Type.NON) {
				relation.addNotification(response);
			}

		} // else no observe was requested or the resource does not allow it
		lower().sendResponse(exchange, response);
	}

	/**
	 * Returns true if the specified response is still in transit. A response is
	 * in transit if it has not yet been acknowledged, rejected or its current
	 * transmission has not yet timed out.
	 */
	private static boolean isInTransit(final Response response) {
		Type type = response.getType();
		boolean acked = response.isAcknowledged();
		boolean timeout = response.isTimedOut();
		boolean result = type == Type.CON && !acked && !timeout;
		return result;
	}

	@Override
	public void receiveResponse(final Exchange exchange, final Response response) {

		if (response.isNotification() && exchange.getRequest().isCanceled()) {
			// The request was canceled and we no longer want notifications
			LOGGER.debug("rejecting notification for canceled Exchange");
			EmptyMessage rst = EmptyMessage.newRST(response);
			sendEmptyMessage(exchange, rst);
			// Matcher sets exchange as complete when RST is sent
		} else {
			// No observe option in response => always deliver
			upper().receiveResponse(exchange, response);
		}
	}

	@Override
	public void receiveEmptyMessage(final Exchange exchange, final EmptyMessage message) {
		// NOTE: We could also move this into the MessageObserverAdapter from
		// sendResponse into the method rejected().
		if (message.getType() == Type.RST && exchange.getOrigin() == Origin.REMOTE) {
			// The response has been rejected
			ObserveRelation relation = exchange.getRelation();
			if (relation != null) {
				relation.cancel();
			} // else there was no observe relation ship and this layer ignores
				// the rst
		}
		upper().receiveEmptyMessage(exchange, message);
	}

	private void prepareSelfReplacement(Exchange exchange, Response response) {
		response.addMessageObserver(new NotificationController(exchange));
	}

	/**
	 * Sends the next CON as soon as the former CON is no longer in transit.
	 */
	private class NotificationController extends MessageObserverAdapter {

		private Exchange exchange;

		public NotificationController(Exchange exchange) {
			this.exchange = exchange;
		}

		@Override
		public void onAcknowledgement() {
			ObserveRelation relation = exchange.getRelation();
			final Response next = relation.getNextControlNotification();
			relation.setCurrentControlNotification(next);
			// next may be null
			relation.setNextControlNotification(null);
			if (next != null) {
				LOGGER.debug("notification has been acknowledged, send the next one");
				/*
				 * The matcher must be able to find the NON notifications to
				 * remove them from the exchangesByMID hashmap
				 */
				if (next.getType() == Type.NON) {
					relation.addNotification(next);
				}
				// Create a new task for sending next response so that we
				// can leave the sync-block
				exchange.execute(new Runnable() {

					@Override
					public void run() {
						ObserveLayer.super.sendResponse(exchange, next);
					}
				});
			}
		}

		@Override
		public void onRetransmission() {
			// called within the exchange executor context.
			ObserveRelation relation = exchange.getRelation();
			final Response next = relation.getNextControlNotification();
			if (next != null) {
				LOGGER.debug("notification has timed out and there is a fresher notification for the retransmission");
				// Cancel the original retransmission and 
				// send the fresh notification here
				exchange.getCurrentResponse().cancel();
				// Convert all notification retransmissions to CON
				if (next.getType() != Type.CON) {
					next.setType(Type.CON);
					prepareSelfReplacement(exchange, next);
				}
				relation.setCurrentControlNotification(next);
				relation.setNextControlNotification(null);
				// Create a new task for sending next response so that we
				// can leave the sync-block
				ObserveLayer.super.sendResponse(exchange, next);
			}
		}

		@Override
		public void onTimeout() {
			ObserveRelation relation = exchange.getRelation();
			LOGGER.info("notification for token [{}] timed out. Canceling all relations with source [{}]",
					relation.getExchange().getRequest().getToken(), relation.getSource());
			relation.cancelAll();
		}

		// Cancellation on RST is done in receiveEmptyMessage()
	}
}
