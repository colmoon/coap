package com.coap.elements;

import com.coap.elements.util.StringUtil;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * An endpoint context that explicitly supports DTLS specific properties.
 */
public class DtlsEndpointContext extends MapBasedEndpointContext {

	/**
	 * The name of the attribute that contains the DTLS session ID.
	 */
	public static final String KEY_SESSION_ID = "DTLS_SESSION_ID";
	/**
	 * The name of the attribute that contains the <em>epoch</em> of the
	 * DTLS session.
	 */
	public static final String KEY_EPOCH = "DTLS_EPOCH";
	/**
	 * The name of the attribute that contains the cipher suite used with
	 * the DTLS session.
	 */
	public static final String KEY_CIPHER = "DTLS_CIPHER";
	/**
	 * The name of the attribute that contains the timestamp of the last
	 * handshake of the DTLS session.
	 */
	public static final String KEY_HANDSHAKE_TIMESTAMP = "KEY_HANDSHAKE_TIMESTAMP";
	/**
	 * The name of the attribute that contains a auto session resumption timeout
	 * in milliseconds. {@code "0"}, force a session resumption, {@code ""},
	 * disable auto session resumption. None critical attribute, not considered
	 * for matching.
	 */
	public static final String KEY_RESUMPTION_TIMEOUT = KEY_PREFIX_NONE_CRITICAL + "DTLS_RESUMPTION_TIMEOUT";

	/**
	 * Creates a context for DTLS session parameters.
	 * 
	 * @param peerAddress peer address of endpoint context
	 * @param peerIdentity peer identity of endpoint context
	 * @param sessionId the session's ID.
	 * @param epoch the session's current read/write epoch.
	 * @param cipher the cipher suite of the session's current read/write state.
	 * @param timestamp the timestamp in milliseconds of the last handshake. See
	 *            {@link System#currentTimeMillis()}.
	 * @throws NullPointerException if any of the parameters other than
	 *             peerIdentity are {@code null}.
	 */
	public DtlsEndpointContext(InetSocketAddress peerAddress, Principal peerIdentity,
							   String sessionId, String epoch, String cipher, String timestamp) {

		this(peerAddress, null, peerIdentity, sessionId, epoch, cipher, timestamp);
	}

	/**
	 * Creates a context for DTLS session parameters.
	 * 
	 * @param peerAddress peer address of endpoint context
	 * @param virtualHost the name of the virtual host at the peer
	 * @param peerIdentity peer identity of endpoint context
	 * @param sessionId the session's ID.
	 * @param epoch the session's current read/write epoch.
	 * @param cipher the cipher suite of the session's current read/write state.
	 * @param timestamp the timestamp in milliseconds of the last handshake. See
	 *            {@link System#currentTimeMillis()}.
	 * @throws NullPointerException if any of the parameters other than
	 *             peerIdentity are {@code null}.
	 */
	public DtlsEndpointContext(InetSocketAddress peerAddress, String virtualHost, Principal peerIdentity,
			String sessionId, String epoch, String cipher, String timestamp) {

		super(peerAddress, virtualHost, peerIdentity, KEY_SESSION_ID, sessionId, KEY_CIPHER, cipher, KEY_EPOCH, epoch,
				KEY_HANDSHAKE_TIMESTAMP, timestamp);
	}

	/**
	 * Gets the identifier of the DTLS session.
	 * 
	 * @return The identifier.
	 */
	public final String getSessionId() {
		return get(KEY_SESSION_ID);
	}

	/**
	 * Gets the current epoch of the DTLS session.
	 * 
	 * @return The epoch number.
	 */
	public final String getEpoch() {
		return get(KEY_EPOCH);
	}

	/**
	 * Gets the name of the cipher suite in use for the DTLS session.
	 * 
	 * @return The name.
	 */
	public final String getCipher() {
		return get(KEY_CIPHER);
	}

	/**
	 * Gets the timestamp in milliseconds of the last handshake.
	 * 
	 * @return The timestamp in milliseconds of the last handshake.
	 * 
	 * @see System#currentTimeMillis()
	 */
	public final String getHandshakeTimestamp() {
		return get(KEY_HANDSHAKE_TIMESTAMP);
	}

	@Override
	public String toString() {
		return String.format("DTLS(%s,ID:%s)", getPeerAddressAsString(),
				StringUtil.trunc(getSessionId(), ID_TRUNC_LENGTH));
	}
}
