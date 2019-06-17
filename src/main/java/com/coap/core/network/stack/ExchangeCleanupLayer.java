package com.coap.core.network.stack;

/**
 * A layer that reacts to user cancelled outgoing requests or messages which
 * failed to be send, and completes exchange, which causes state clean up.
 */
public class ExchangeCleanupLayer extends AbstractLayer {

	static final Logger LOGGER = LoggerFactory.getLogger(ExchangeCleanupLayer.class.getName());

	/**
	 * Multicast lifetime in milliseconds.
	 */
	private final int multicastLifetime;

	public ExchangeCleanupLayer(final NetworkConfig config) {
		this.multicastLifetime = config.getInt(NetworkConfig.Keys.NON_LIFETIME)
				+ config.getInt(NetworkConfig.Keys.MAX_LATENCY)
				+ config.getInt(NetworkConfig.Keys.MAX_SERVER_RESPONSE_DELAY);
	}

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
		if (request.isMulticast()) {
			request.addMessageObserver(new MulticastCleanupMessageObserver(exchange, executor, multicastLifetime));
		} else {
			request.addMessageObserver(new CleanupMessageObserver(exchange));
		}
		super.sendRequest(exchange, request);
	}

	/**
	 * Adds a message observer to a confirmable response to be sent which
	 * completes the exchange if the response gets canceled or failed.
	 * 
	 * @param exchange The (remotely originating) exchange that the response is
	 *            part of.
	 * @param response The outbound response.
	 */
	@Override
	public void sendResponse(final Exchange exchange, final Response response) {

		if (response.isConfirmable() && !response.isNotification()) {
			response.addMessageObserver(new CleanupMessageObserver(exchange));
		}
		super.sendResponse(exchange, response);
	}

	@Override
	public void receiveResponse(final Exchange exchange, final Response response) {
		if (!exchange.getRequest().isMulticast()) {
			// multicast exchanges are completed with MulticastCleanupMessageObserver
			exchange.setComplete();
			exchange.getRequest().onComplete();
		}
		super.receiveResponse(exchange, response);
	}

}
