package com.coap.elements.exception;

/**
 * Exception indicating, that the endpoint context doesn't match for some
 * reason.
 */
public class EndpointMismatchException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new instance.
	 */
	public EndpointMismatchException() {
		super();
	}

	/**
	 * Create new instance with message.
	 * 
	 * @param message message
	 */
	public EndpointMismatchException(String message) {
		super(message);
	}
}
