package com.coap.core.network.interceptors;

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MessageTracer logs all incoming and outgoing messages. MessageInterceptor
 * are located between the serializer/parser and the matcher. Each message comes
 * or goes through a connector is logged.
 */
public class MessageTracer implements MessageInterceptor {

	private final static Logger LOGGER = LoggerFactory.getLogger(MessageTracer.class.getCanonicalName());

	@Override
	public void sendRequest(Request request) {
		LOGGER.info("{} <== req {}", new Object[]{request.getDestinationContext(), request});
	}

	@Override
	public void sendResponse(Response response) {
		LOGGER.info("{} <== res {}", new Object[]{response.getDestinationContext(), response});
	}

	@Override
	public void sendEmptyMessage(EmptyMessage message) {
		LOGGER.info("{} <== emp {}", new Object[]{message.getDestinationContext(), message});
	}

	@Override
	public void receiveRequest(Request request) {
		LOGGER.info("{} ==> req {}", new Object[]{request.getSourceContext(), request});
	}

	@Override
	public void receiveResponse(Response response) {
		LOGGER.info("{} ==> res {}", new Object[]{response.getSourceContext(), response});
	}

	@Override
	public void receiveEmptyMessage(EmptyMessage message) {
		LOGGER.info("{} ==> emp {}", new Object[]{message.getSourceContext(), message});
	}
}
