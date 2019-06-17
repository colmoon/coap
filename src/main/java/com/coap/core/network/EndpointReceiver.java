package com.coap.core.network;

/**
 * Endpoint message receiver. Passes exchange and message to endpoints
 * protocol-stack.
 */
public interface EndpointReceiver {

	/**
	 * Process received request.
	 * 
	 * @param exchange exchange of request
	 * @param request received request
	 */
	void receiveRequest(Exchange exchange, Request request);

	/**
	 * Process received response.
	 * 
	 * @param exchange exchange of response
	 * @param response received response
	 */
	void receiveResponse(Exchange exchange, Response response);

	/**
	 * Process received empty message.
	 * 
	 * @param exchange exchange of empty message
	 * @param message received empty message
	 */
	void receiveEmptyMessage(Exchange exchange, EmptyMessage message);

	/**
	 * Reject (received) message.
	 * 
	 * @param message received message to reject
	 */
	void reject(Message message);
}
