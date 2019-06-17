package com.coap.elements;

/**
 * Relaxed endpoint context matcher. Matches DTLS without epoch.
 */
public class RelaxedDtlsEndpointContextMatcher extends KeySetEndpointContextMatcher {

	private static final String KEYS[] = { DtlsEndpointContext.KEY_SESSION_ID, DtlsEndpointContext.KEY_CIPHER };

	/**
	 * Creates a new matcher.
	 * <p>
	 * Two contexts will be considered <em>matching</em> if they have the same value
	 * for properties
	 * <ul>
	 *   <li>{@link DtlsEndpointContext#KEY_SESSION_ID}</li>
	 *   <li>{@link DtlsEndpointContext#KEY_CIPHER}</li>
	 * </ul>
	 * and have a matching virtualHost property according to
	 * {@link KeySetEndpointContextMatcher#isSameVirtualHost(EndpointContext, EndpointContext)}.
	 */
	public RelaxedDtlsEndpointContextMatcher() {
		super("relaxed context", KEYS, true);
	}
}
