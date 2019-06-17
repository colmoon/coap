package com.coap.elements;

/**
 * Endpoint context matcher for UDP.
 * 
 * Optionally checks address for request-response matching.
 */
public class UdpEndpointContextMatcher extends KeySetEndpointContextMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpEndpointContextMatcher.class.getName());

	private static final String KEYS[] = { UdpEndpointContext.KEY_PLAIN };

	/**
	 * Enable address check for request-response matching.
	 */
	private final boolean checkAddress;

	/**
	 * Create new instance of udp endpoint context matcher with enabled address
	 * check.
	 */
	public UdpEndpointContextMatcher() {
		this(true);
	}

	/**
	 * Create new instance of udp endpoint context matcher.
	 * 
	 * @param checkAddress {@code true} with address check, {@code false},
	 *            without
	 */
	public UdpEndpointContextMatcher(boolean checkAddress) {
		super("udp plain", KEYS);
		this.checkAddress = checkAddress;
	}

	@Override
	public boolean isResponseRelatedToRequest(EndpointContext requestContext, EndpointContext responseContext) {
		if (checkAddress) {
			InetSocketAddress peerAddress1 = requestContext.getPeerAddress();
			InetSocketAddress peerAddress2 = responseContext.getPeerAddress();
			if (peerAddress1.getPort() != peerAddress2.getPort() || (!peerAddress1.getAddress().isMulticastAddress()
					&& !peerAddress1.getAddress().equals(peerAddress2.getAddress()))) {
				LOGGER.info("request {}:{} doesn't match {}:{}!", peerAddress1.getAddress().getHostAddress(),
						peerAddress1.getPort(), peerAddress2.getAddress().getHostAddress(), peerAddress2.getPort());
				return false;
			}
		}
		return super.isResponseRelatedToRequest(requestContext, responseContext);
	}
}
