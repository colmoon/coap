package com.coap.core.network;

/**
 * @ClassName Exchange
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/13 21:27
 * @Version 1.0
 **/

import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.core.observe.ObserveRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An exchange represents the complete state of an exchange of one request and
 * one or more responses. The lifecycle of an exchange ends when either the last
 * response has arrived and is acknowledged, when a request or response has been
 * rejected from the remote endpoint, when the request has been canceled, or
 * when a request or response timed out, i.e., has reached the retransmission
 * limit without being acknowledged.
 * <p>
 * The framework internally uses the class Exchange to manage an exchange of
 * {@link Request}s and {@link Response}s. The Exchange only contains state, no
 * functionality(功能). The CoAP Stack contains the functionality of the CoAP protocol
 * and modifies the exchange appropriately. The class Exchange and its fields
 * are <em>NOT</em> thread-safe. The setter methods must be called within a
 * {@link Runnable}, which must be executed using {@link #execute(Runnable)}.
 * For convenience the {@link #executeComplete()} is provided to execute
 * {@link #setComplete()} accordingly(因此). Methods, which are documented to throw a
 * {@link ConcurrentModificationException} MUST comply(遵守，服从，顺从) to this execution
 * pattern!
 * <p>
 * If the exchange represents a "blockwise" transfer and if the transparent(透明的) mode
 * is used, the exchange keeps also the (original) request and use the current
 * request for transfer the blocks. A request not using observe use the same
 * token for easier tracking. A request using observe keeps the origin request
 * with the origin token in store, but use a different token for the transfer of
 * the left blocks. This enables to catch new notifies while a transfer is
 * ongoing.
 * <p>
 * The class {@link CoapExchange} provides the corresponding(相应的) API for developers.
 * Proceed with caution when using this class directly, e.g., through
 * {@link CoapExchange#advanced()}.
 * <p>
 * This class might change with the implementation of CoAP extensions.
 * <p>
 * Even if above mentions, that this class is not thread safe, its used from
 * several different threads! Generally the Exchanges are hand over via a
 * concurrent collections in the matcher and therefore establish a "happens
 * before" order (as long as threads accessing the exchange via the matcher).
 * But some methods are out of scope of that and use Exchange directly (e.g.
 * {@link #setEndpointContext(EndpointContext)} the "sender thread" or
 * {@link #setFailedTransmissionCount(int)} the "retransmission thread
 * (executor)"). Therefore use at least volatile for the fields. This doesn't
 * ensure, that Exchange is thread safe, it only ensures the visibility of the
 * states.
 */

public class Exchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(Exchange.class.getName());

    static final boolean DEBUG = LOGGER.isTraceEnabled();

    private static final int MAX_OBSERVE_NO = (1 << 24) - 1;

    /**
     * ID generator for logging messages.
     */
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

    /**
     * The origin of an exchange.
     * <p>
     * If Cf receives a new request and creates a new exchange the origin is
     * REMOTE since the request has been initiated from a remote endpoint. If Cf
     * creates a new request and sends it, the origin is LOCAL.
     */
    public enum Origin {

        /**
         * Indicates that a message exchange has been initiated locally.
         */
        LOCAL,

        /**
         * Indicates that a message exchange has been initiated remotely.
         */
        REMOTE;
    }

    /**
     * ID for logging.
     */
    private final int id;

    /**
     * Executor for exchange jobs.
     *
     * Note: for unit tests this may be {@code null} to escape the owner checking.
     * Otherwise many change in the tests would be required.
     */
    private final SerialExecutor executor;

    /**
     * Caller of {@link #setComplete()}. Intended for debug logging.
     */
    private Throwable caller;

    /**
     * The endpoint that processes this exchange.
     *
     * Set on receiving a message.
     */
    private volatile Endpoint endpoint;

    /** An remove handler to be called when a exchange must be removed from the exchange store */
    private volatile RemoveHandler removeHandler;

    /** Indicates if the exchange is complete */
    private final AtomicBoolean complete = new AtomicBoolean();

    /** The nano timestamp when this exchange has been created */
    private final long nanoTimestamp;

    /**
     * Enable to keep the original request in the exchange store. Intended to be
     * used for observe request with blockwise response to be able to react on
     * newer notifies during an ongoing transfer.
     */
    private final boolean keepRequestInStore;

    /**
     * Mark exchange as notification.
     */
    private final boolean notification;

    /**
     * The actual request that caused this exchange. Layers below the
     * {@link BlockwiseLayer} should only work with the {@link #currentRequest}
     * while layers above should work with the {@link #request}.
     */
    // the initial request we have to exchange
    private volatile Request request;

    /**
     * The current block of the request that is being processed. This is a
     * single block in case of a blockwise transfer or the same as
     * {@link #request} in case of a normal transfer.
     */
    // Matching needs to know for what we expect a response
    private volatile Request currentRequest;

    /**
     * The actual response that is supposed to be sent to the client. Layers
     * below the {@link BlockwiseLayer} should only work with the
     * {@link #currentResponse} while layers above should work with the
     * {@link #response}.
     */
    private volatile Response response;

    /** The current block of the response that is being transferred. */
    // Matching needs to know when receiving duplicate
    private volatile Response currentResponse;

    // indicates where the request of this exchange has been initiated.
    // (as suggested by effective Java, item 40.)
    private final Origin origin;

    // true if the exchange has failed due to a timeout
    private volatile boolean timedOut;

    // the timeout of the current request or response set by reliability(可靠性) layer
    private volatile int currentTimeout;

    // handle to cancel retransmission
    private ScheduledFuture<?> retransmissionHandle;

    // If the request was sent with a block1 option the response has to send its
    // first block piggy-backed(在...基础上) with the Block1 option of the last request block
    private volatile BlockOption block1ToAck;

    private volatile Integer notificationNumber;

    // The relation that the target resource has established with the source
    private volatile ObserveRelation relation;

    private final AtomicReference<EndpointContext> endpointContext = new AtomicReference<EndpointContext>();

    //If object security option is used, the Cryptographic context identifier(标识符) is stored here
    // for request/response mapping of contexts
    private byte[] cryptoContextId;

    /**
     * Creates a new exchange with the specified request and origin.
     *
     * @param request the request that starts the exchange
     * @param origin the origin of the request (LOCAL or REMOTE)
     * @param executor executor to be used for exchanges. Maybe {@code null} for unit tests.
     * @throws NullPointerException, if request is {@code null}
     */
    public Exchange(Request request, Origin origin, Executor executor) {
        this(request, origin, executor, null, request != null && request.isObserve(), false);
    }

    /**
     * Creates a new exchange with the specified request, origin, context, and
     * notification marker.
     *
     * @param request the request that starts the exchange
     * @param origin the origin of the request (LOCAL or REMOTE)
     * @param executor executor to be used for exchanges. Maybe {@code null} for unit tests.
     * @param ctx the endpoint context of this exchange
     * @param notification {@code true} for notification exchange, {@code false}
     *            otherwise
     * @throws NullPointerException, if request is {@code null}
     */
    public Exchange(Request request, Origin origin, Executor executor, EndpointContext ctx, boolean notification) {
        this(request, origin, executor, ctx, request != null && request.isObserve() && !notification, notification);
    }

    /**
     * Creates a new exchange with the specified request, origin and context.
     *
     * @param request the request that starts the exchange
     * @param origin the origin of the request (LOCAL or REMOTE)
     * @param executor executor to be used for exchanges. Maybe {@code null} for unit tests.
     * @param ctx the endpoint context of this exchange
     * @param keepRequestInStore {@code true}, to keep the original request in
     *            store until completed, {@code false} otherwise.
     * @param notification {@code true} for notification exchange, {@code false}
     *            otherwise
     * @throws NullPointerException, if request is {@code null}
     */
    private Exchange(Request request, Origin origin, Executor executor, EndpointContext ctx, boolean keepRequestInStore,
                     boolean notification) {
        // might only be the first block of the whole request
        if (request == null) {
            throw new NullPointerException("request must not be null!");
        }
        this.id = INSTANCE_COUNTER.incrementAndGet();
        this.executor = SerialExecutor.create(executor);
        this.currentRequest = request;
        this.request = request;
        this.origin = origin;
        this.endpointContext.set(ctx);
        this.keepRequestInStore = keepRequestInStore;
        this.notification = notification;
        this.nanoTimestamp = ClockUtil.nanoRealtime();
    }

    @Override
    public String toString() {
        char originMarker = origin == Origin.LOCAL ? 'L' : 'R';
        if (complete.get()) {
            return "Exchange[" + originMarker + id + ", complete]";
        } else {
            return "Exchange[" + originMarker + id + "]";
        }
    }

    /**
     * Accept this exchange and therefore the request. Only if the request's
     * type was a <code>CON</code> and the request has not been acknowledged
     * yet, it sends an ACK to the client.
     */
    public void sendAccept() {
        assert (origin == Origin.REMOTE);
        Request current = currentRequest;
        if (current.getType() == Type.CON && current.hasMID() && !current.isAcknowledged()) {
            current.setAcknowledged(true);
            EmptyMessage ack = EmptyMessage.newACK(current);
            endpoint.sendEmptyMessage(this, ack);
        }
    }

    /**
     * Reject this exchange and therefore the request. Sends an RST back to the
     * client.
     */
    public void sendReject() {
        assert (origin == Origin.REMOTE);
        Request current = currentRequest;
        if (current.hasMID() && !current.isRejected()) {
            current.setRejected(true);
            EmptyMessage rst = EmptyMessage.newRST(current);
            endpoint.sendEmptyMessage(this, rst);
        }
    }

    /**
     * Sends the specified response over the same endpoint as the request has
     * arrived.
     *
     * @param response the response
     */
    public void sendResponse(Response response) {
        Request current = currentRequest;
        response.setDestinationContext(current.getSourceContext());
        endpoint.sendResponse(this, response);
    }

    //TODO

}
