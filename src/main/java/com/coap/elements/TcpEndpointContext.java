package com.coap.elements;

import com.coap.elements.util.StringUtil;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * A endpoint context that explicitly supports TCP specific properties.
 */
public class TcpEndpointContext extends MapBasedEndpointContext {

	/**
	 * Key for TCP connection ID.
	 * 
	 */
	public static final String KEY_CONNECTION_ID = "CONNECTION_ID";

	/**
	 * Creates a new endpoint context from TCP connection ID.
	 * 
	 * @param peerAddress peer address of endpoint context
	 * @param connectionId the connectionn's ID.
	 * @throws NullPointerException if connectionId or peer address is
	 *             <code>null</code>.
	 */
	public TcpEndpointContext(InetSocketAddress peerAddress, String connectionId) {
		this(peerAddress, null, KEY_CONNECTION_ID, connectionId);
	}

	/**
	 * Creates a new endpoint context.
	 * 
	 * Intended to be used by subclasses, which provides a principal and
	 * additional attributes. The {@link #KEY_CONNECTION_ID} attribute MUST be
	 * included in the attributes list.
	 * 
	 * @param peerAddress peer address of endpoint context
	 * @param peerIdentity peer identity of endpoint context
	 * @param attributes list of attributes (name-value pairs, e.g. key_1,
	 *            value_1, key_2, value_2 ...), the pair
	 *            {@link #KEY_CONNECTION_ID}, "id" must be contained in the
	 *            attributes.
	 */
	protected TcpEndpointContext(InetSocketAddress peerAddress, Principal peerIdentity, String... attributes) {
		super(peerAddress, peerIdentity, attributes);
		if (null == getConnectionId()) {
			throw new IllegalArgumentException("Missing attribute ");
		}
	}

	public String getConnectionId() {
		return get(KEY_CONNECTION_ID);
	}

	@Override
	public String toString() {
		return String.format("TCP(%s,ID:%s)", getPeerAddressAsString(),
				StringUtil.trunc(getConnectionId(), ID_TRUNC_LENGTH));
	}

}
