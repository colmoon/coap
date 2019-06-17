package com.coap.elements;

/**
 * Strict endpoint context matcher. Uses strictly matching for DTLS including
 * the security epoch.
 */
public class StrictDtlsEndpointContextMatcher extends KeySetEndpointContextMatcher {

	private static final String KEYS[] = { DtlsEndpointContext.KEY_SESSION_ID, DtlsEndpointContext.KEY_EPOCH,
			DtlsEndpointContext.KEY_CIPHER };

	/**
	 * Creates a new matcher.
	 * <p>
	 * Two contexts will be considered <em>matching</em> if they have the same value
	 * for properties
	 * <ul>
	 *   <li>{@link DtlsEndpointContext#KEY_SESSION_ID}</li>
	 *   <li>{@link DtlsEndpointContext#KEY_EPOCH}</li>
	 *   <li>{@link DtlsEndpointContext#KEY_CIPHER}</li>
	 * </ul>
	 * and have a matching virtualHost property according to
	 * {@link KeySetEndpointContextMatcher#isSameVirtualHost(EndpointContext, EndpointContext)}.
	 */
	public StrictDtlsEndpointContextMatcher() {
		super("strict context", KEYS, true);
	}
}
