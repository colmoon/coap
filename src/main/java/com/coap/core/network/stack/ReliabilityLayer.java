package com.coap.core.network.stack;

import com.coap.core.coap.*;
import com.coap.core.coap.CoAP.Type;
import com.coap.core.network.Exchange;
import com.coap.core.network.config.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The reliability layer. CON retransmission. ACK/RST processing.
 */
public class ReliabilityLayer extends AbstractLayer {

	/** The logger. */
	protected final static Logger LOGGER = LoggerFactory.getLogger(ReliabilityLayer.class.getCanonicalName());

	/** The random numbers generator for the back-off timer */
	private final Random rand = new Random();

	private final int ack_timeout;
	private final float ack_random_factor;
	private final float ack_timeout_scale;
	private final int max_retransmit;

	/**
	 * Constructs a new reliability layer. Changes to the configuration are
	 * observed and automatically applied.
	 * 
	 * @param config the configuration
	 */
	public ReliabilityLayer(final NetworkConfig config) {
		ack_timeout = config.getInt(NetworkConfig.Keys.ACK_TIMEOUT);
		ack_random_factor = config.getFloat(NetworkConfig.Keys.ACK_RANDOM_FACTOR);
		ack_timeout_scale = config.getFloat(NetworkConfig.Keys.ACK_TIMEOUT_SCALE);
		max_retransmit = config.getInt(NetworkConfig.Keys.MAX_RETRANSMIT);

		LOGGER.info("ReliabilityLayer uses ACK_TIMEOUT={}, ACK_RANDOM_FACTOR={}, and ACK_TIMEOUT_SCALE={}",
				new Object[] { ack_timeout, ack_random_factor, ack_timeout_scale });
	}

	/**
	 * Schedules a retransmission for confirmable messages.
	 */
	@Override
	public void sendRequest(final Exchange exchange, final Request request) {

		LOGGER.debug("{} send request, failed transmissions: {}", exchange, exchange.getFailedTransmissionCount());

		if (request.getType() == null) {
			request.setType(Type.CON);
		}
		if (request.getType() == Type.CON) {
			LOGGER.debug("{} prepare retransmission for {}", exchange, request);
			prepareRetransmission(exchange, new RetransmissionTask(exchange, request) {

				public void retransmit() {
					sendRequest(exchange, request);
				}
			});
		}
		lower().sendRequest(exchange, request);
	}

	/**
	 * Makes sure that the response type is correct. The response type for a NON
	 * can be NON or CON. The response type for a CON should either be an ACK
	 * with a piggy-backed response or, if an empty ACK has already be sent, a
	 * CON or NON with a separate response.
	 */
	@Override
	public void sendResponse(final Exchange exchange, final Response response) {

		LOGGER.debug("{} send response {}, failed transmissions: {}", exchange, response,
				exchange.getFailedTransmissionCount());

		// If a response type is set, we do not mess around with it.
		// Only if none is set, we have to decide for one here.

		Type respType = response.getType();
		if (respType == null) {
			Type reqType = exchange.getCurrentRequest().getType();
			if (reqType == Type.CON) {
				if (exchange.getCurrentRequest().isAcknowledged()) {
					// send separate response
					response.setType(Type.CON);
				} else {
					exchange.getCurrentRequest().setAcknowledged(true);
					// send piggy-backed response
					response.setType(Type.ACK);
					response.setMID(exchange.getCurrentRequest().getMID());
				}
			} else {
				// send NON response
				response.setType(Type.NON);
			}

			LOGGER.trace("{} switched response message type from {} to {} (request was {})", exchange, respType,
					response.getType(), reqType);

		} else if (respType == Type.ACK || respType == Type.RST) {
			response.setMID(exchange.getCurrentRequest().getMID());
		}

		if (response.getType() == Type.CON) {
			LOGGER.debug("{} prepare retransmission for {}", exchange, response);
			prepareRetransmission(exchange, new RetransmissionTask(exchange, response) {

				public void retransmit() {
					sendResponse(exchange, response);
				}
			});
		}
		lower().sendResponse(exchange, response);
	}

	/**
	 * Computes the back-off timer and schedules the specified retransmission
	 * task.
	 * 
	 * @param exchange the exchange
	 * @param task the retransmission task
	 */
	private void prepareRetransmission(final Exchange exchange, final RetransmissionTask task) {

		// prevent RejectedExecutionException
		if (executor.isShutdown()) {
			LOGGER.info("Endpoint is being destroyed: skipping retransmission");
			return;
		}

		exchange.setRetransmissionHandle(null); // cancel before reschedule
		updateRetransmissionTimeout(exchange);

		task.message.addMessageObserver(new MessageObserverAdapter() {

			@Override
			public void onSent() {
				task.message.removeMessageObserver(this);
				if (!exchange.isComplete()) {
					exchange.execute(new Runnable() {

						@Override
						public void run() {
							task.startTimer();
						}
					});
				}
			}

		});
	}

	/**
	 * When we receive a duplicate of a request, we stop it here and do not
	 * forward it to the upper layer. If the server has already sent a response,
	 * we send it again. If the request has only been acknowledged (but the ACK
	 * has gone lost or not reached the client yet), we resent the ACK. If the
	 * request has neither been responded, acknowledged or rejected yet, the
	 * server has not yet decided what to do with the request and we cannot do
	 * anything.
	 */
	@Override
	public void receiveRequest(final Exchange exchange, final Request request) {

		if (request.isDuplicate()) {
			// Request is a duplicate, so resend ACK, RST or response
			exchange.retransmitResponse();
			Response currentResponse = exchange.getCurrentResponse();
			if (currentResponse != null) {
				if (currentResponse.getType() == Type.NON || currentResponse.getType() == Type.CON) {
					// separate response
					if (request.isConfirmable()) {
						// resend ACK,
						// comply to RFC 7252, 4.2, cross-layer behavior
						EmptyMessage ack = EmptyMessage.newACK(request);
						sendEmptyMessage(exchange, ack);
					}
					if (currentResponse.isConfirmable()) {
						// retransmission cycle
						int failedCount = exchange.getFailedTransmissionCount() + 1;
						exchange.setFailedTransmissionCount(failedCount);
						LOGGER.debug("{} request duplicate: retransmit response, failed: {}, response: {}", exchange,
								failedCount, currentResponse);
						currentResponse.retransmitting();
						sendResponse(exchange, currentResponse);
						return;
					}
				}
				LOGGER.debug("{} respond with the current response to the duplicate request", exchange);
				// Do not restart retransmission cycle
				lower().sendResponse(exchange, currentResponse);

			} else if (exchange.getCurrentRequest().isAcknowledged()) {
				LOGGER.debug("{} duplicate request was acknowledged but no response computed yet. Retransmit ACK",
						exchange);
				EmptyMessage ack = EmptyMessage.newACK(request);
				sendEmptyMessage(exchange, ack);

			} else if (exchange.getCurrentRequest().isRejected()) {
				LOGGER.debug("{} duplicate request was rejected. Reject again", exchange);
				EmptyMessage rst = EmptyMessage.newRST(request);
				sendEmptyMessage(exchange, rst);

			} else {
				LOGGER.debug("{} server has not yet decided what to do with the request. We ignore the duplicate.",
						exchange);
				// The server has not yet decided, whether to acknowledge or
				// reject the request. We know for sure that the server has
				// received the request though and can drop this duplicate here.
			}

		} else {
			// Request is not a duplicate
			exchange.setCurrentRequest(request);
			upper().receiveRequest(exchange, request);
		}
	}

	/**
	 * When we receive a Confirmable response, we acknowledge it and it also
	 * counts as acknowledgment for the request. If the response is a duplicate,
	 * we stop it here and do not forward it to the upper layer.
	 */
	@Override
	public void receiveResponse(final Exchange exchange, final Response response) {

		exchange.setFailedTransmissionCount(0);
		exchange.setRetransmissionHandle(null);
		exchange.getCurrentRequest().setAcknowledged(true);

		if (response.getType() == Type.CON && !exchange.getRequest().isCanceled()) {
			LOGGER.debug("{} acknowledging CON response", exchange);
			EmptyMessage ack = EmptyMessage.newACK(response);
			sendEmptyMessage(exchange, ack);
		}

		if (response.isDuplicate()) {
			LOGGER.debug("{} ignoring duplicate response", exchange);
		} else {
			upper().receiveResponse(exchange, response);
		}
	}

	/**
	 * If we receive an ACK or RST, we mark the outgoing request or response as
	 * acknowledged or rejected respectively and cancel its retransmission.
	 */
	@Override
	public void receiveEmptyMessage(final Exchange exchange, final EmptyMessage message) {

		exchange.setFailedTransmissionCount(0);
		exchange.setRetransmissionHandle(null);
		// TODO: If this is an observe relation, the current response might not
		// be the one that is being acknowledged. The current response might
		// already be the next NON notification.
		String type;
		Message currentMessage;
		if (exchange.isOfLocalOrigin()) {
			type = "request";
			currentMessage = exchange.getCurrentRequest();
		} else {
			type = "response";
			currentMessage = exchange.getCurrentResponse();
		}
		int observer = currentMessage.getMessageObservers().size();
		if (message.getType() == Type.ACK) {
			LOGGER.debug("{} acknowledge {} for {} {} ({} msg observer)", exchange, message, type, currentMessage,
					observer);
			currentMessage.setAcknowledged(true);
		} else if (message.getType() == Type.RST) {
			LOGGER.debug("{} reject {} for {} {} ({} msg observer)", exchange, message, type, currentMessage, observer);
			currentMessage.setRejected(true);
		} else {
			LOGGER.warn("{} received empty message that is neither ACK nor RST: {}", exchange, message);
			return;
		}

		upper().receiveEmptyMessage(exchange, message);
	}

	/**
	 * Update the exchange's current timeout.
	 * 
	 * Prepares either for the first transmission or stretches timeout for
	 * follow-up retransmissions.
	 * 
	 * @param exchange exchange to update the current timeout
	 * @see Exchange#getCurrentTimeout()
	 * @see Exchange#setCurrentTimeout(int)
	 * @see Exchange#getFailedTransmissionCount()
	 */
	protected void updateRetransmissionTimeout(final Exchange exchange) {
		int timeout;
		if (exchange.getFailedTransmissionCount() == 0) {
			/*
			 * For a new confirmable message, the initial timeout is set to a
			 * random number between ACK_TIMEOUT and (ACK_TIMEOUT *
			 * ACK_RANDOM_FACTOR)
			 */
			timeout = getRandomTimeout(ack_timeout, (int) (ack_timeout * ack_random_factor));
		} else {
			timeout = (int) (ack_timeout_scale * exchange.getCurrentTimeout());
		}
		exchange.setCurrentTimeout(timeout);
	}

	/**
	 * Returns a random timeout between the specified min and max.
	 * 
	 * @param min the min
	 * @param max the max
	 * @return a random value between min and max
	 */
	protected int getRandomTimeout(final int min, final int max) {
		if (min >= max) {
			return min;
		}
		synchronized (rand) {
			return min + rand.nextInt(max - min + 1);
		}
	}

	/*
	 * The main reason to create this class was to enable the methods
	 * sendRequest and sendResponse to use the same code for sending messages
	 * but where the retransmission method calls sendRequest and sendResponse
	 * respectively.
	 */
	protected abstract class RetransmissionTask implements Runnable {

		private final Exchange exchange;
		private final Message message;

		public RetransmissionTask(final Exchange exchange, final Message message) {
			this.exchange = exchange;
			this.message = message;
		}

		public void startTimer() {
			if (!exchange.isComplete()) {
				int timeout = exchange.getCurrentTimeout();
				ScheduledFuture<?> f = executor.schedule(this, timeout, TimeUnit.MILLISECONDS);
				exchange.setRetransmissionHandle(f);
			}
		}

		@Override
		public void run() {
			exchange.execute(new Runnable() {

				@Override
				public void run() {
					retry();
				}
			});
		}

		private void retry() {
			/*
			 * Do not retransmit a message if it has been acknowledged,
			 * rejected, canceled or already been retransmitted for the maximum
			 * number of times.
			 */
			try {
				exchange.setRetransmissionHandle(null);
				if (exchange.isComplete()) {
					LOGGER.debug("Timeout: for {}, {}", exchange, message);
					return;
				}
				int failedCount = exchange.getFailedTransmissionCount() + 1;
				exchange.setFailedTransmissionCount(failedCount);

				LOGGER.debug("Timeout: for {} retry {} of {}", exchange, failedCount, message);

				if (message.isAcknowledged()) {
					LOGGER.trace("Timeout: for {} message already acknowledged, cancel retransmission of {}", exchange,
							message);
					return;

				} else if (message.isRejected()) {
					LOGGER.trace("Timeout: for {} message already rejected, cancel retransmission of {}", exchange,
							message);
					return;

				} else if (message.isCanceled()) {
					LOGGER.trace("Timeout: for {}, {} is canceled, do not retransmit", exchange, message);
					return;

				} else if (failedCount <= max_retransmit) {
					LOGGER.debug("Timeout: for {} retransmit message, failed: {}, message: {}", exchange, failedCount,
							message);

					// Trigger MessageObservers
					message.retransmitting();

					// MessageObserver might have canceled
					if (message.isCanceled()) {
						LOGGER.trace("Timeout: for {}, {} got canceled, do not retransmit", exchange, message);
						return;
					}
					retransmit();
				} else {
					LOGGER.debug("Timeout: for {} retransmission limit reached, exchange failed, message: {}", exchange,
							message);
					exchange.setTimedOut(message);
				}
			} catch (Exception e) {
				LOGGER.error("Exception for {} in MessageObserver: {}", exchange, e.getMessage(), e);
			}
		}

		public abstract void retransmit();
	}

}
