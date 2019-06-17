package com.coap.core.network.stack;

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import com.coap.core.server.MessageDeliverer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * CoapStack is what CoapEndpoint uses to send messages through distinct layers.
 */
public interface CoapStack {

	// delegate to top
	void sendRequest(Exchange exchange, Request request);

	// delegate to top
	void sendResponse(Exchange exchange, Response response);

	// delegate to top
	void sendEmptyMessage(Exchange exchange, EmptyMessage message);

	// delegate to bottom
	void receiveRequest(Exchange exchange, Request request);

	// delegate to bottom
	void receiveResponse(Exchange exchange, Response response);

	// delegate to bottom
	void receiveEmptyMessage(Exchange exchange, EmptyMessage message);

	void setExecutor(ScheduledExecutorService executor);

	void setDeliverer(MessageDeliverer deliverer);

	void destroy();

	boolean hasDeliverer();
}
