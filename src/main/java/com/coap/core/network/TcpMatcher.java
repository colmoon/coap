package com.coap.core.network;

import com.coap.core.coap.*;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.observe.NotificationListener;
import com.coap.core.observe.ObservationStore;
import com.coap.core.observe.ObserveRelation;
import com.coap.elements.EndpointContext;
import com.coap.elements.EndpointContextMatcher;
import com.coap.core.network.Exchange.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Matcher that runs over reliable TCP/TLS protocol. Based on
 * <a href="https://tools.ietf.org/html/draft-ietf-core-coap-tcp-tls-02"/>
 */
public final class TcpMatcher extends BaseMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpMatcher.class.getName());
	private final RemoveHandler exchangeRemoveHandler = new RemoveHandlerImpl();
	private final EndpointContextMatcher endpointContextMatcher;

	/**
	 * Creates a new matcher for running CoAP over TCP.
	 * 
	 * @param config the configuration to use.
	 * @param notificationListener the callback to invoke for notifications
	 *            received from peers.
	 * @param tokenGenerator token generator to create tokens for observations
	 *            created by the endpoint this matcher is part of.
	 * @param observationStore the object to use for keeping track of
	 *            observations created by the endpoint this matcher is part of.
	 * @param exchangeStore The store to use for keeping track of message
	 *            exchanges.
	 * @param executor executor to be used for exchanges.
	 * @param endpointContextMatcher endpoint context matcher to relate
	 *            responses with requests
	 * @throws NullPointerException if one of the parameters is {@code null}.
	 */
	public TcpMatcher(NetworkConfig config, NotificationListener notificationListener, TokenGenerator tokenGenerator,
					  ObservationStore observationStore, MessageExchangeStore exchangeStore, Executor executor,
					  EndpointContextMatcher endpointContextMatcher) {
		super(config, notificationListener, tokenGenerator, observationStore, exchangeStore, executor);
		this.endpointContextMatcher = endpointContextMatcher;
	}

	@Override
	public void sendRequest(Exchange exchange) {

		Request request = exchange.getCurrentRequest();
		if (request.isObserve()) {
			registerObserve(request);
		}
		exchange.setRemoveHandler(exchangeRemoveHandler);
		exchangeStore.registerOutboundRequestWithTokenOnly(exchange);
		LOGGER.debug("tracking open request using {}", request.getTokenString());
	}

	@Override
	public void sendResponse(Exchange exchange) {
		final Response response = exchange.getCurrentResponse();
		final ObserveRelation observeRelation = exchange.getRelation();

		// ensure Token is set
		response.setToken(exchange.getCurrentRequest().getToken());

		if (observeRelation != null) {
			response.addMessageObserver(new MessageObserverAdapter() {

				@Override
				public void onSendError(Throwable error) {
					observeRelation.cancel();
				}
			});
		}

		exchange.setComplete();
	}

	@Override
	public void sendEmptyMessage(Exchange exchange, EmptyMessage message) {
		// ensure Token is set
		if (message.isConfirmable()) {
			message.setToken(Token.EMPTY);
		} else {
			throw new UnsupportedOperationException("sending empty message (ACK/RST) over tcp is not supported!");
		}
	}

	@Override
	public void receiveRequest(final Request request, final EndpointReceiver receiver) {

		final Exchange exchange = new Exchange(request, Exchange.Origin.REMOTE, executor);
		exchange.setRemoveHandler(exchangeRemoveHandler);
		exchange.execute(new Runnable() {

			@Override
			public void run() {
				receiver.receiveRequest(exchange, request);
			}
		});
	}

	@Override
	public void receiveResponse(final Response response, final EndpointReceiver receiver) {

		final Token idByToken = response.getToken();
		Exchange tempExchange = exchangeStore.get(idByToken);

		if (tempExchange == null) {
			// we didn't find a message exchange for the token from the response
			// let's try to find an existing observation for the token
			tempExchange = matchNotifyResponse(response);
		}

		if (tempExchange == null) {
			// There is no exchange with the given token - ignore response
			LOGGER.trace("discarding unmatchable response from [{}]: {}", response.getSourceContext(), response);
			return;
		}

		final Exchange exchange = tempExchange;
		exchange.execute(new Runnable() {

			@Override
			public void run() {
				boolean checkResponseToken = !exchange.isNotification() || exchange.getRequest() != exchange.getCurrentRequest();
				if (checkResponseToken && exchangeStore.get(idByToken) != exchange) {
					if (running) {
						LOGGER.error("ignoring response {}, exchange not longer matching!", response);
					}
					return;
				}

				EndpointContext context = exchange.getEndpointContext();
				if (context == null) {
					// ignore response
					LOGGER.error("ignoring response from [{}]: {}, request pending to sent!",
							response.getSourceContext(), response);
					return;
				}
				try {
					if (endpointContextMatcher.isResponseRelatedToRequest(context, response.getSourceContext())) {
						receiver.receiveResponse(exchange, response);
					} else {
						LOGGER.debug(
								"ignoring potentially forged response from [{}]: {} for {} with non-matching endpoint context",
								response.getSourceContext(), response, exchange);
					}
				} catch (Exception ex) {
					LOGGER.error("error receiving response from [{}]: {} for {}", response.getSourceContext(), response,
							exchange, ex);
				}
			}
		});
	}

	@Override
	public void receiveEmptyMessage(final EmptyMessage message, EndpointReceiver receiver) {
		/* ignore received empty messages via tcp */
	}

	private class RemoveHandlerImpl implements RemoveHandler {

		@Override
		public void remove(Exchange exchange, Token token, KeyMID key) {
			if (token != null) {
				exchangeStore.remove(token, exchange);
			}
			// ignore key, MID is not used for TCP!
		}
	}
}
