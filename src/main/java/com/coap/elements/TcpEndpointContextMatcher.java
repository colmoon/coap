package com.coap.elements;

/**
 * TCP endpoint context matcher.
 */
public class TcpEndpointContextMatcher extends KeySetEndpointContextMatcher {

	private static final String KEYS[] = { TcpEndpointContext.KEY_CONNECTION_ID };

	public TcpEndpointContextMatcher() {
		super("tcp context", KEYS);
	}
}
