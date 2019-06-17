package com.coap.elements.exception;

/**
 * Exception indicating, that the connector doesn't support multicast messages.
 */
public class MulticastNotSupportedException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new instance.
	 */
	public MulticastNotSupportedException() {
		super();
	}

	/**
	 * Create new instance with message.
	 * 
	 * @param message message
	 */
	public MulticastNotSupportedException(String message) {
		super(message);
	}
}
