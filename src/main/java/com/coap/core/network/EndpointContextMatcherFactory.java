package com.coap.core.network;

import com.coap.core.coap.CoAP;
import com.coap.core.network.config.NetworkConfig;
import com.coap.elements.*;

/**
 * Factory for endpoint context matcher.
 */
public class EndpointContextMatcherFactory {

	public enum MatcherMode {
		STRICT, RELAXED, PRINCIPAL
	}

	/**
	 * Create endpoint context matcher related to connector according the
	 * configuration.
	 * 
	 * If connector supports "coaps:", RESPONSE_MATCHING is used to determine,
	 * if {@link StrictDtlsEndpointContextMatcher},
	 * {@link RelaxedDtlsEndpointContextMatcher}, or
	 * {@link PrincipalEndpointContextMatcher} is used.
	 * 
	 * If connector supports "coap:", RESPONSE_MATCHING is used to determine, if
	 * {@link UdpEndpointContextMatcher} is used with disabled
	 * ({@link MatcherMode#RELAXED}) or enabled address check (otherwise).
	 * 
	 * For other protocol flavors the corresponding matcher is used.
	 * 
	 * @param connector connector to create related endpoint context matcher.
	 * @param config configuration.
	 * @return endpoint context matcher
	 */
	public static EndpointContextMatcher create(Connector connector, NetworkConfig config) {
		String protocol = null;
		if (null != connector) {
			protocol = connector.getProtocol();
			if (CoAP.PROTOCOL_TCP.equalsIgnoreCase(protocol)) {
				return new TcpEndpointContextMatcher();
			} else if (CoAP.PROTOCOL_TLS.equalsIgnoreCase(protocol)) {
				return new TlsEndpointContextMatcher();
			}
		}
		String textualMode = "???";
		MatcherMode mode = MatcherMode.STRICT;
		try {
			textualMode = config.getString(NetworkConfig.Keys.RESPONSE_MATCHING);
			mode = MatcherMode.valueOf(textualMode);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Response matching mode '" + textualMode + "' not supported!");
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Response matching mode not provided/configured!");
		}
		switch (mode) {
		case RELAXED:
			if (CoAP.PROTOCOL_UDP.equalsIgnoreCase(protocol)) {
				return new UdpEndpointContextMatcher(false);
			} else {
				return new RelaxedDtlsEndpointContextMatcher();
			}
		case PRINCIPAL:
			if (CoAP.PROTOCOL_UDP.equalsIgnoreCase(protocol)) {
				return new UdpEndpointContextMatcher(false);
			} else {
				return new PrincipalEndpointContextMatcher();
			}
		case STRICT:
		default:
			if (CoAP.PROTOCOL_UDP.equalsIgnoreCase(protocol)) {
				return new UdpEndpointContextMatcher(true);
			} else {
				return new StrictDtlsEndpointContextMatcher();
			}
		}
	}
}
