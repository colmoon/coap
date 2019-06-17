package com.coap.core.network.stack;

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP adaption layer. Set acknowledged on response receiving.
 */
public class TcpAdaptionLayer extends AbstractLayer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TcpAdaptionLayer.class.getName());

	@Override
	public void sendEmptyMessage(final Exchange exchange, final EmptyMessage message) {

		if (message.isConfirmable()) {
			// CoAP over TCP uses empty messages as pings for keep alive.
			// TODO: Should we instead rely on TCP keep-alives configured via TCP Connector?
			lower().sendEmptyMessage(exchange, message);
		} else {
			// Empty messages don't make sense when running over TCP connector.
			LOGGER.warn("attempting to send empty message (ACK/RST) in TCP mode {} - {}", message, exchange.getCurrentRequest(), new Throwable());
		}
	}

	@Override
	public void receiveRequest(final Exchange exchange, final Request request) {
		request.setAcknowledged(true);
		upper().receiveRequest(exchange, request);
	}

	@Override
	public void receiveResponse(Exchange exchange, Response response) {
		response.setAcknowledged(true);
		upper().receiveResponse(exchange, response);
	}

	@Override
	public void receiveEmptyMessage(Exchange exchange, EmptyMessage message) {
		// Empty messages are ignored when running over TCP connector.
		LOGGER.info("discarding empty message received in TCP mode: {}", message);
	}

}
