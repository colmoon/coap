package com.coap.core.network.stack;

import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layer that reacts to user cancelled outgoing requests or messages which
 * failed to be send, and completes exchange, which causes state clean up.
 */
public class TcpExchangeCleanupLayer extends AbstractLayer {

	static final Logger LOGGER = LoggerFactory.getLogger(TcpExchangeCleanupLayer.class.getName());

	/**
	 * Adds a message observer to the request to be sent which completes the
	 * exchange if the request gets canceled or failed.
	 * 
	 * @param exchange The (locally originating) exchange that the request is
	 *            part of.
	 * @param request The outbound request.
	 */
	@Override
	public void sendRequest(final Exchange exchange, final Request request) {

		request.addMessageObserver(new CleanupMessageObserver(exchange));
		super.sendRequest(exchange, request);
	}

	/**
	 * Complete exchange when response is received.
	 */
	@Override
	public void receiveResponse(final Exchange exchange, final Response response) {
		exchange.setComplete();
		exchange.getRequest().onComplete();
		super.receiveResponse(exchange, response);
	}
}
