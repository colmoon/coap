package com.coap.elements;

import com.coap.elements.util.StringUtil;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * A endpoint context that explicitly supports TLS specific properties.
 * Currently the context is not aware of renegotiation (API to acquire
 * information is missing). According oracle, the renegotiate issues seems to be
 * fixed, if your java is not to deprecated.
 * 
 * @see <a href=
 *      "http://www.oracle.com/technetwork/java/javase/overview/tlsreadme2-176330.html">
 *      Fix renegotiate</a>
 */
public class TlsEndpointContext extends TcpEndpointContext {

	public static final String KEY_SESSION_ID = "DTLS_SESSION_ID";

	public static final String KEY_CIPHER = "CIPHER";

	/**
	 * Creates a new correlation context from TLS session parameters.
	 * 
	 * @param peerAddress peer address of endpoint context
	 * @param peerIdentity peer identity of endpoint context
	 * @param connectionId the connectionn's ID.
	 * @param sessionId the session's ID.
	 * @param cipher the cipher suite of the session's current read/write state.
	 * @throws NullPointerException if any of the params is <code>null</code>.
	 */
	public TlsEndpointContext(InetSocketAddress peerAddress, Principal peerIdentity, String connectionId,
							  String sessionId, String cipher) {
		super(peerAddress, peerIdentity, KEY_CONNECTION_ID, connectionId, KEY_SESSION_ID, sessionId, KEY_CIPHER,
				cipher);
	}

	public String getSessionId() {
		return get(KEY_SESSION_ID);
	}

	public String getCipher() {
		return get(KEY_CIPHER);
	}

	@Override
	public String toString() {
		return String.format("TLS(%s,%s,%s,%s)", getPeerAddressAsString(),
				StringUtil.trunc(getConnectionId(), ID_TRUNC_LENGTH), StringUtil.trunc(getSessionId(), ID_TRUNC_LENGTH),
				getCipher());
	}

}
