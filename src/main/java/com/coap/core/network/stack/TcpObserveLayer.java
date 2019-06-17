package com.coap.core.network.stack;

import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.observe.ObserveRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP related observe/notify handling.
 * No CON/NON logic possible nor required.
 */
public class TcpObserveLayer extends AbstractLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpObserveLayer.class.getName());

	private static final Integer CANCEL = 1;

	/**
	 * Creates a new observe layer for a configuration.
	 * 
	 * @param config The configuration values to use.
	 */
	public TcpObserveLayer(final NetworkConfig config) {
		// so far no configuration values for this layer
	}

	@Override
	public void sendRequest(final Exchange exchange, final Request request) {
		if (CANCEL.equals(request.getOptions().getObserve())) {
			/* TODO: don't send, if connection is not available */
		}
		lower().sendRequest(exchange, request);
	}

	@Override
	public void sendResponse(final Exchange exchange, final Response response) {
		final ObserveRelation relation = exchange.getRelation();
		if (relation != null && relation.isEstablished()) {
			if (!response.getOptions().hasObserve()) {
				/* response for cancel request */
				relation.cancel();
			}
		} // else no observe was requested or the resource does not allow it
		lower().sendResponse(exchange, response);
	}

	@Override
	public void receiveResponse(final Exchange exchange, final Response response) {
		if (response.getOptions().hasObserve() && exchange.getRequest().isCanceled()) {
			// The request was canceled and we no longer want notifications
			LOGGER.debug("ignoring notification for canceled TCP Exchange");
		} else {
			// No observe option in response => always deliver
			upper().receiveResponse(exchange, response);
		}
	}
}
