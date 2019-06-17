package com.coap.core.network.interceptors;

/**
 * @ClassName MessageInterceptor
 * @Description MessageInterceptor
 * @Author wuxiaojian
 * @Date 2019/6/13 22:07
 * @Version 1.0
 **/

import com.coap.core.coap.EmptyMessage;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;

/**
 * MessageInterceptors register at an endpoint. When messages arrive from the
 * connector, the corresponding receive-method is called. When a message is
 * about to be sent over a connector, the corresponding send method is called.
 * The interceptor can be thought of being placed inside an <code>CoapEndpoint</code>
 * just between the message <code>Serializer</code> and the <code>Matcher</code>.
 * <p>
 * A <code>MessageInterceptor</code> can cancel a message to stop it. If it is
 * an outgoing message that traversed down through the <code>CoapStack</code> to the
 * <code>Matcher</code> and is now intercepted and canceled, will not reach the
 * <code>Connector</code>. If it is an incoming message coming from the
 * <code>Connector</code> to the <code>DataParser</code> and is now intercepted and
 * canceled, will not reach the <code>Matcher</code>.
 */
public interface MessageInterceptor {

    /**
     * Override this method to be notified when a request is about to be sent.
     *
     * @param request the request
     */
    void sendRequest(Request request);

    /**
     * Override this method to be notified when a response is about to be sent.
     *
     * @param response the response
     */
    void sendResponse(Response response);

    /**
     * Override this method to be notified when an empty message is about to be
     * sent.
     *
     * @param message the empty message
     */
    void sendEmptyMessage(EmptyMessage message);

    /**
     * Override this method to be notified when request has been received.
     *
     * @param request the request
     */
    void receiveRequest(Request request);

    /**
     * Override this method to be notified when response has been received.
     *
     * @param response the response
     */
    void receiveResponse(Response response);

    /**
     * Override this method to be notified when an empty message has been
     * received.
     *
     * @param message the message
     */
    void receiveEmptyMessage(EmptyMessage message);
}

