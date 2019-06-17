package com.coap.elements;

/**
 * @ClassName AddressEndpointContext
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/17 14:11
 * @Version 1.0
 **/

import com.coap.elements.util.StringUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

/**
 * A endpoint context providing the inet socket address and a optional
 * principal.
 */
public class AddressEndpointContext implements EndpointContext{

    protected static final int ID_TRUNC_LENGTH = 6;

    private final InetSocketAddress peerAddress;

    private final Principal peerIdentity;

    private final String virtualHost;

    /**
     * Creates a context for an IP address and port.
     *
     * @param address IP address of peer
     * @param port port of peer
     * @throws NullPointerException if provided address is {@code null}.
     */
    public AddressEndpointContext(InetAddress address, int port) {
        if (address == null) {
            throw new NullPointerException("missing peer inet address!");
        }
        this.peerAddress = new InetSocketAddress(address, port);
        this.peerIdentity = null;
        this.virtualHost = null;
    }

    /**
     * Creates a context for a socket address.
     *
     * @param peerAddress socket address of peer's service
     * @throws NullPointerException if provided peer address is {@code null}.
     */
    public AddressEndpointContext(InetSocketAddress peerAddress) {
        this(peerAddress, null, null);
    }

    /**
     * Creates a context for a socket address and an authenticated identity.
     *
     * @param peerAddress socket address of peer's service
     * @param peerIdentity peer's principal
     * @throws NullPointerException if provided peer address is {@code null}.
     */
    public AddressEndpointContext(InetSocketAddress peerAddress, Principal peerIdentity) {
        this(peerAddress, null, peerIdentity);
    }

    /**
     * Create endpoint context with principal.
     *
     * @param peerAddress socket address of peer's service
     * @param virtualHost the name of the virtual host at the peer
     * @param peerIdentity peer's principal
     * @throws NullPointerException if provided peer address is {@code null}.
     */
    public AddressEndpointContext(InetSocketAddress peerAddress, String virtualHost, Principal peerIdentity) {
        if (peerAddress == null) {
            throw new NullPointerException("missing peer socket address, must not be null!");
        }
        this.peerAddress = peerAddress;
        this.virtualHost = virtualHost == null ? null : virtualHost.toLowerCase();
        this.peerIdentity = peerIdentity;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null}
     */
    @Override
    public String get(String key) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @return an empty map
     */
    @Override
    public Map<String, String> entries() {
        return Collections.emptyMap();
    }

    @Override
    public boolean hasCriticalEntries() {
        return false;
    }

    @Override
    public final Principal getPeerIdentity() {
        return peerIdentity;
    }

    @Override
    public final InetSocketAddress getPeerAddress() {
        return peerAddress;
    }

    @Override
    public final String getVirtualHost() {
        return virtualHost;
    }

    @Override
    public String toString() {
        return String.format("IP(%s)", getPeerAddressAsString());
    }

    protected final String getPeerAddressAsString() {
        return StringUtil.toString(peerAddress);
    }

}
