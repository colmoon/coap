package com.coap.core.network.interceptors;

import com.coap.core.coap.CoAP.*;
import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interceptor which logs the source IP addresses of incoming requests.
 * <p>
 * In order to make proper use of this interceptor, the CoAP server should
 * be started with the <em>logback-sandbox.xml</em> logback configuration file
 * in the project's base directory.
 * <p>
 * This can be done by means of setting the <em>logback.configurationFile</em>
 * system property on the command line when starting the JVM, e.g.:
 * <pre>
 * java -Dlogback.configurationFile=/path/to/logback.sandbox.xml ...
 * </pre>
 * <p>
 * The gathered data is used for the Eclipse IoT metrics.
 */
public final class OriginTracer extends MessageInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(OriginTracer.class);

	@Override
	public void receiveRequest(Request request) {
		LOGGER.trace("{}", request.getSourceContext().getPeerAddress());
	}

	@Override
	public void receiveEmptyMessage(EmptyMessage message) {
		// only log pings
		if (message.getType() == Type.CON) {
			LOGGER.trace("{}", message.getSourceContext().getPeerAddress());
		}
	}
}
