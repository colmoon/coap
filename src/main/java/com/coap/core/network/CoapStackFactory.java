package com.coap.core.network;

/**
 * Factory for CoapStack.
 * 
 * Either provided to the {@link CoapEndpoint.Builder} or set as
 * default {@link CoapEndpoint#setDefaultCoapStackFactory(CoapStackFactory)}.
 */
public interface CoapStackFactory {

	/**
	 * Create CoapStack.
	 * 
	 * @param protocol used protocol, values see
	 *            {@link Connector#getProtocol()}.
	 * @param config network configuration used for this coap stack
	 * @param outbox outbox to be used for this coap stack
	 * @return create coap stack-
	 * @throws NullPointerException if any parameter is {@code null}
	 * @throws IllegalArgumentException if protocol is not supported.
	 */
	CoapStack createCoapStack(String protocol, NetworkConfig config, Outbox outbox);
}
