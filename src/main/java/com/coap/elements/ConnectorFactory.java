package com.coap.elements;

/**
 * A factory for {@link Connector} objects.
 * 
 * An implementation will usually create one type of Connectors only, e.g.
 * standard unencrypted UDP connectors vs. encrypted DTLS based connectors.
 */
public interface ConnectorFactory {

	/**
	 * Creates a new network connector.
	 * 
	 * The connectors created by this method are <em>not</em> started yet.
	 * 
	 * @param socketAddress the IP address and port to connect to
	 * @return the connector
	 */
	Connector newConnector(InetSocketAddress socketAddress);
}
