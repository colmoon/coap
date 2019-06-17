package com.coap.elements;

/**
 * TLS endpoint context matcher.
 */
public class TlsEndpointContextMatcher extends KeySetEndpointContextMatcher {

	private static final String KEYS[] = { TcpEndpointContext.KEY_CONNECTION_ID, TlsEndpointContext.KEY_SESSION_ID,
			TlsEndpointContext.KEY_CIPHER };

	public TlsEndpointContextMatcher() {
		super("tls context", KEYS);
	}
}
