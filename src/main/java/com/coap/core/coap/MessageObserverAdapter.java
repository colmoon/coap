package com.coap.core.coap;

/**
 * @ClassName MessageObserverAdapter
 * @Description MessageObserverAdapter
 * @Author wuxiaojian
 * @Date 2019/6/17 14:25
 * @Version 1.0
 **/

import com.coap.elements.EndpointContext;

/**
 * An abstract adapter class for reacting to a message's lifecylce events.
 * <p>
 * The methods in this class are empty, except {@link #onReject()},
 *  {@link #onTimeout()}, and
 * {@link #onSendError(Throwable)}, which are calling {@link #failed()} as
 * default implementation. This class exists as convenience for creating message
 * observer objects.
 * <p>
 * Subclasses should override the methods for the events of interest.
 * <p>
 * An instance of the concrete message observer can then be registered with a
 * message using the message's <code>addMessageObserver</code> method.
 */
public abstract class MessageObserverAdapter implements MessageObserver {

    @Override
    public void onRetransmission() {
        // empty default implementation
    }

    @Override
    public void onResponse(final Response response) {
        // empty default implementation
    }

    @Override
    public void onAcknowledgement() {
        // empty default implementation
    }

    @Override
    public void onReject() {
        failed();
    }

    @Override
    public void onCancel() {
        // empty default implementation
    }

    @Override
    public void onTimeout() {
        failed();
    }

    @Override
    public void onReadyToSend() {
        // empty default implementation
    }

    @Override
    public void onConnecting() {
        // empty default implementation
    }

    @Override
    public void onDtlsRetransmission(int flight) {
        // empty default implementation
    }

    @Override
    public void onSent() {
        // empty default implementation
    }

    @Override
    public void onSendError(Throwable error) {
        failed();
    }

    @Override
    public void onContextEstablished(EndpointContext endpointContext) {
        // empty default implementation
    }

    @Override
    public void onComplete() {
        // empty default implementation
    }

    /**
     * Common method to be overwritten to catch failed messages.
     *
     * @see #onReject()
     * @see #onTimeout()
     * @see #onSendError(Throwable)
     */
    protected void failed() {
        // empty default implementation
    }

}

