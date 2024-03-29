package com.coap.elements.auth;

import com.coap.elements.util.StringUtil;

import java.security.Principal;

/**
 * A principal representing an authenticated peer's identity as used in a
 * <em>pre-shared key</em> handshake.
 */
public final class PreSharedKeyIdentity implements Principal {

	private final boolean scopedIdentity;
	private final String virtualHost;
	private final String identity;
	private final String name;

	/**
	 * Creates a new instance for an identity.
	 * 
	 * @param identity the identity
	 * @throws NullPointerException if the identity is <code>null</code>
	 */
	public PreSharedKeyIdentity(String identity) {
		this(false, null, identity);
	}

	/**
	 * Creates a new instance for an identity scoped to a virtual host.
	 * 
	 * @param virtualHost The virtual host name that the identity is scoped to.
	 *                    The host name will be converted to lower case.
	 * @param identity the identity.
	 * @throws NullPointerException if the identity is <code>null</code>
	 * @throws IllegalArgumentException if virtual host is not a valid host name
	 *             as per <a href="http://tools.ietf.org/html/rfc1123">RFC 1123</a>.
	 */
	public PreSharedKeyIdentity(String virtualHost, String identity) {
		this(true, virtualHost, identity);
	}

	/**
	 * Creates a new instance for an identity optional scoped to a virtual host.
	 * 
	 * @param sni enable scope to a virtual host
	 * @param virtualHost The virtual host name that the identity is scoped to.
	 *            The host name will be converted to lower case.
	 * @param identity the identity.
	 * @throws NullPointerException if the identity is <code>null</code>
	 * @throws IllegalArgumentException if virtual host is not a valid host name
	 *             as per <a href="http://tools.ietf.org/html/rfc1123">RFC
	 *             1123</a>.
	 */
	private PreSharedKeyIdentity(boolean sni, String virtualHost, String identity) {
		if (identity == null) {
			throw new NullPointerException("Identity must not be null");
		} else {
			scopedIdentity = sni;
			StringBuilder b = new StringBuilder();
			if (sni) {
				if (virtualHost == null) {
					this.virtualHost = null;
				} else if (StringUtil.isValidHostName(virtualHost)) {
					this.virtualHost = virtualHost.toLowerCase();
					b.append(this.virtualHost);
				} else {
					throw new IllegalArgumentException("virtual host is not a valid hostname");
				}
				b.append(":");
			} else {
				if (virtualHost != null) {
					throw new IllegalArgumentException("virtual host is not supported, if sni is disabled");
				}
				this.virtualHost = null;
			}
			this.identity = identity;

			b.append(identity);
			this.name = b.toString();
		}
	}

	/**
	 * Checks, if the identity is scoped by the virtual host name.
	 * 
	 * @return {@code true}, if the identity is scoped by the virtual host name.
	 */
	public boolean isScopedIdentity() {
		return scopedIdentity;
	}

	/**
	 * Gets the virtual host name that the identity is scoped to.
	 * 
	 * @return The name or {@code null} if not set.
	 */
	public String getVirtualHost() {
		return virtualHost;
	}

	/**
	 * Gets the identity.
	 * 
	 * @return The identity.
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * Gets the name of this principal.
	 * <p>
	 * If the identity is not scoped by the server name, the
	 * {@link #getIdentity()} is returned. If the identity is scoped by the
	 * server name, the name consists of that server name and the identity,
	 * separated by a colon character. If no server name has been provided, then
	 * the name consists of a colon character followed by the identity.
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Gets a string representation of this principal.
	 * 
	 * Clients should not assume any particular format of the returned string
	 * since it may change over time.
	 * 
	 * @return the string representation
	 */
	@Override
	public String toString() {
		if (scopedIdentity) {
			return new StringBuilder("PreSharedKey Identity [").append("virtual host: ").append(virtualHost)
					.append(", identity: ").append(identity).append("]").toString();
		} else {
			return new StringBuilder("PreSharedKey Identity [").append("identity: ").append(identity).append("]")
					.toString();
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Compares another object to this identity.
	 * 
	 * @return {@code true} if the other object is a {@code PreSharedKeyIdentity} and
	 *         its name property has the same value as this instance.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PreSharedKeyIdentity other = (PreSharedKeyIdentity) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
