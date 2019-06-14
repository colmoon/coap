package com.coap.core.network;

/**
 * @ClassName Exchange
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/13 21:27
 * @Version 1.0
 **/

import java.util.ConcurrentModificationException;

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

}
