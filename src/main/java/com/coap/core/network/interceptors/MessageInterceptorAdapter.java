package com.coap.core.network.interceptors;

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;

/**
 * An abstract adapter class for reacting to a message's transfer events.
 * <p>
 * The methods in this class are empty.
 * <p>
 * Subclasses should override the methods for the events of interest.
 * <p>
 * An instance of the concrete message intercepter can then be registered with a
 * <code>CoapEndpoint</code> using <code>addInterceptor</code> method.
 * 
 * @see CoapEndpoint#addInterceptor(MessageInterceptor)
 */
public abstract class MessageInterceptorAdapter implements MessageInterceptor {

	@Override
	public void sendRequest(Request request) {
		// empty default implementation
	}

	@Override
	public void sendResponse(Response response) {
		// empty default implementation
	}

	@Override
	public void sendEmptyMessage(EmptyMessage message) {
		// empty default implementation
	}

	@Override
	public void receiveRequest(Request request) {
		// empty default implementation
	}

	@Override
	public void receiveResponse(Response response) {
		// empty default implementation
	}

	@Override
	public void receiveEmptyMessage(EmptyMessage message) {
		// empty default implementation
	}

}
