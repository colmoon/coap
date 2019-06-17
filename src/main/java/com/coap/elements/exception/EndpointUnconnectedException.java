package com.coap.elements.exception;

/**
 * Exception indicating, that the destination endpoint is currently not
 * connected to the source server endpoint. Used for TCP/TLS server and for DTLS
 * server, if the DTLS server is configured to act as server only and therefore
 * not starting handshakes.
 */
public class EndpointUnconnectedException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new instance.
	 */
	public EndpointUnconnectedException() {
		super();
	}

	/**
	 * Create new instance with message.
	 * 
	 * @param message message
	 */
	public EndpointUnconnectedException(String message) {
		super(message);
	}
}
