package com.coap.core.network;

/**
 * Exception indicating, that the exchange is already complete.
 */
public class ExchangeCompleteException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create new instance with message.
	 * 
	 * @param message message
	 * @param caller caller of {@link Exchange#setComplete()}
	 */
	public ExchangeCompleteException(String message, Throwable caller) {
		super(message, caller);
	}
}
