package com.coap.core.coap;

/**
 * @ClassName MessageObserver
 * @Description MessageObserver
 * @Author wuxiaojian
 * @Date 2019/6/17 13:38
 * @Version 1.0
 **/

import com.coap.elements.EndpointContext;

/**
 * A callback that gets invoked on a message's life cycle events.
 * <p>
 * The following methods are called
 * <ul>
 * <li>{@link #onResponse(Response)} when a response arrives</li>
 * <li>{@link #onAcknowledgement()} when the message has been acknowledged</li>
 * <li>{@link #onReject()} when the message has been rejected</li>
 * <li>{@link #onTimeout()} when the client stops retransmitting the message and
 * still has not received anything from the remote endpoint</li>
 * <li>{@link #onCancel()} when the message has been canceled</li>
 * <li>{@link #onReadyToSend()} right before the message is being sent</li>
 * <li>{@link #onConnecting()} right before a connector establish a connection.
 * Not called, if the connection is already established or the connector doesn't
 * require to establish a connection.</li>
 * <li>{@link #onDtlsRetransmission(int)} when a dtls handshake flight is retransmitted.</li>
 * <li>{@link #onSent()} right after the message has been sent
 * (successfully)</li>
 * <li>{@link #onSendError(Throwable)} if the message cannot be sent</li>
 * </ul>
 * <p>
 * The class that is interested in processing a message event either implements
 * this interface (and all the methods it contains) or extends the abstract
 * {@link MessageObserverAdapter} class (overriding only the methods of
 * interest).
 * <p>
 * The observer object created from that class is then registered with a message
 * using the message's {@link Message#addMessageObserver(MessageObserver)}
 * method.
 * <p>
 * Note: This class is unrelated to CoAP's observe relationship between an
 * endpoint and a resource. However, when a request establishes a CoAP observe
 * relationship to a resource which sends notifications, the method
 * {@link #onResponse(Response)} can be used to react to each such notification.
 */
public interface MessageObserver {
    /**
     * Invoked when a message is about to be re-transmitted.
     */
    void onRetransmission();

    /**
     * Invoked when a response arrives.
     *
     * @param response the response that arrives
     */
    void onResponse(Response response);

    /**
     * Invoked when the message has been acknowledged by the remote endpoint.
     */
    void onAcknowledgement();

    /**
     * Invoked when the message has been rejected by the remote endpoint.
     */
    void onReject();

    /**
     * Invoked when the client stops retransmitting the message and still has
     * not received anything from the remote endpoint.
     * <p>
     * By default this is the case after 5 unsuccessful transmission attempts.
     */
    void onTimeout();

    /**
     * Invoked when the message has been canceled.
     * <p>
     * For instance, a user might cancel a request or a CoAP resource that is
     * being observed might cancel a response to send another one instead.
     */
    void onCancel();

    /**
     * Invoked when the message was built and is ready to be sent.
     * <p>
     * Triggered, before the message was sent by a connector. MID and token is
     * prepared.
     */
    void onReadyToSend();

    /**
     * Invoked, when connector requires to establish a connection before sending
     * the message.
     */
    void onConnecting();

    /**
     * Indicate, that this message triggered the connector to establish a
     * connection and a dtls handshake flight was retransmitted.
     *
     * @param flight {@code 1 ... 6}, number of retransmitted flight.
     */
    void onDtlsRetransmission(int flight);

    /**
     * Invoked right after the message has been sent.
     * <p>
     * Triggered, when the message was sent by a connector.
     */
    void onSent();

    /**
     * Invoked when sending the message caused an error.
     * <p>
     * For instance, if the message is not sent, because the endpoint context
     * has changed.
     *
     * @param error The cause of the failure to send the message.
     */
    void onSendError(Throwable error);

    /**
     * Invoked when the resulting endpoint context is reported by the connector.
     *
     * Note: usually this callback must be processed in a synchronous manner,
     * because if it returns, the message is sent. Therefore take special care
     * in methods called on this callback.
     *
     * @param endpointContext resulting endpoint context
     */
    void onContextEstablished(EndpointContext endpointContext);

    /**
     * Invoked, when transfer is successfully complete.
     */
    void onComplete();
}
