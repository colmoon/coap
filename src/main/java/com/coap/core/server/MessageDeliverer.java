package com.coap.core.server;

/**
 * A strategy for delivering inbound(入站，到达的) CoAP messages to an appropriate processor.
 *
 * Implementations should try to deliver incoming CoAP requests to a published
 * resource matching the request's URI. If no such resource exists, implementations
 * should respond with a CoAP {@link ResponseCode#NOT_FOUND}. An incoming CoAP response
 * message should be delivered to its corresponding outbound request.
 */

import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;

/**
 * @ClassName MessageDeliverer
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/15 10:00
 * @Version 1.0
 **/
public interface MessageDeliverer {

    /**
     * Delivers an inbound CoAP request to an appropriate resource.
     *
     * @param exchange
     *            the exchange containing the inbound {@code Request}
     * @throws NullPointerException if exchange is {@code null}.
     */
    void deliverRequest(Exchange exchange);

    /**
     * Delivers an inbound CoAP response message to its corresponding request.
     *
     * @param exchange
     *            the exchange containing the originating CoAP request
     * @param response
     *            the inbound CoAP response message
     * @throws NullPointerException if exchange or response are {@code null}.
     * @throws IllegalArgumentException if the exchange does not contain a request.
     */
    void deliverResponse(Exchange exchange, Response response);
}
