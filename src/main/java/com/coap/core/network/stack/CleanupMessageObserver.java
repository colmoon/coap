package com.coap.core.network.stack;

import com.coap.core.coap.MessageObserverAdapter;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleanup exchange when user cancelled outgoing requests or messages which
 * failed to be send.
 */
public class CleanupMessageObserver extends MessageObserverAdapter {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CleanupMessageObserver.class.getName());

	protected final Exchange exchange;

	protected CleanupMessageObserver(final Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public void onCancel() {
		complete("canceled");
	}

	@Override
	public void failed() {
		complete("failed");
	}

	protected void complete(final String action) {
		if (exchange.executeComplete()) {
			if (exchange.isOfLocalOrigin()) {
				Request request = exchange.getCurrentRequest();
				LOGGER.debug("{}, {} request [MID={}, {}]", action, exchange, request.getMID(), request.getToken());
			} else {
				Response response = exchange.getCurrentResponse();
				LOGGER.debug("{}, {} response [MID={}, {}]", action, exchange, response.getMID(), response.getToken());
			}
		}
	}
}
