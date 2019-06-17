package com.coap.core.network;

public interface Outbox {

	/**
	 * Sends the specified request over the connector that the stack is
	 * connected to.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param request
	 *            the request
	 */
	public void sendRequest(Exchange exchange, Request request);

	/**
	 * Sends the specified response over the connector that the stack is
	 * connected to.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param response
	 *            the response
	 */
	public void sendResponse(Exchange exchange, Response response);

	/**
	 * Sends the specified empty message over the connector that the stack is
	 * connected to.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param emptyMessage
	 *            the empty message
	 */
	public void sendEmptyMessage(Exchange exchange, EmptyMessage emptyMessage);
	
}
