package com.coap.elements;

import java.net.InetSocketAddress;

/**
 * A endpoint context for plain UDP.
 */
public class UdpEndpointContext extends MapBasedEndpointContext {

	public static final String KEY_PLAIN = "PLAIN";

	/**
	 * Creates a new context for a socket address.
	 * 
	 * @param peerAddress The peer's address.
	 */
	public UdpEndpointContext(InetSocketAddress peerAddress) {
		super(peerAddress, null, KEY_PLAIN, "");
	}

	@Override
	public String toString() {
		return String.format("UDP(%s)", getPeerAddressAsString());
	}
}
