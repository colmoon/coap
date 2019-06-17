package com.coap.core.network.stack;

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
