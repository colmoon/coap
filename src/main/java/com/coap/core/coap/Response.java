package com.coap.core.coap;


import static com.coap.core.coap.CoAP.*;

/**
 * @ClassName Response
 * @Description Response
 * @Author wuxiaojian
 * @Date 2019/6/13 22:03
 * @Version 1.0
 **/

/**
 * Response represents a CoAP response to a CoAP request.
 * <p>
 * A response is either a <em>piggy-backed</em> response of type {@code ACK} or
 * a <em>separate</em> response of type {@code CON} or {@code NON}.
 * Each response carries a ({@link CoAP.ResponseCode}) indicating the outcome
 * of the request it is the response for.
 *
 * @see Request
 */
public class Response extends Message {

    /** The response code. */
    private final CoAP.ResponseCode code;

    /**
     * RTT (round trip time) in milliseconds.
     */
    private volatile Long rtt;

    /**
     * Creates a response to the provided received request with the specified
     * response code. The destination endpoint context of the response will be
     * the source endpoint context of the request. Type and MID are usually set
     * automatically by the {@link ReliabilityLayer}. The token is set
     * automatically by the {@link Matcher}.
     *
     * @param receivedRequest the request
     * @param code the code
     * @return the response
     * @throws IllegalArgumentException if request has no source endpoint
     *             context.
     */
    public static Response createResponse(Request receivedRequest, ResponseCode code) {
        if (receivedRequest.getSourceContext() == null) {
            throw new IllegalArgumentException("received request must contain a source context.");
        }
        Response response = new Response(code);
        response.setDestinationContext(receivedRequest.getSourceContext());
        return response;
    }

    /**
     * Instantiates a new response with the specified response code.
     *
     * @param code the response code
     */
    public Response(ResponseCode code) {
        this.code = code;
    }

    /**
     * Gets the response code.
     *
     * @return the code
     */
    public CoAP.ResponseCode getCode() {
        return code;
    }

    @Override
    public int getRawCode() {
        return code.value;
    }

    @Override
    public String toString() {
        String payload = getPayloadTracingString();
        return String.format("%s-%-6s MID=%5d, Token=%s, OptionSet=%s, %s", getType(), getCode(), getMID(), getTokenString(), getOptions(), payload);
    }

    /**
     * Return RTT (round trip time).
     *
     * @return RTT in milliseconds, or {@code null}, if not set.
     */
    public Long getRTT() {
        return rtt;
    }

    /**
     * Set RTT (round trip time) .
     *
     * @param rtt round trip time of response in milliseconds
     */
    public void setRTT(long rtt) {
        this.rtt = rtt;
    }

    /**
     * Checks whether this response is a notification for
     * an observed resource.
     *
     * @return {@code true} if this response has the observe option set.
     */
    public boolean isNotification() {
        return getOptions().hasObserve();
    }

    /**
     * Checks whether this response has either a <em>block1</em> or
     * <em>block2</em> option.
     *
     * @return {@code true} if this response has a block option.
     */
    public boolean hasBlockOption() {
        return getOptions().hasBlock1() || getOptions().hasBlock2();
    }

    /**
     * Checks whether this response's code indicates an error.
     *
     * @return {@code true} if <em>code</em> indicates an error.
     */
    public final boolean isError() {
        return isClientError() || isServerError();
    }

    /**
     * Checks whether this response's code indicates a client error.
     *
     * @return {@code true} if <em>code</em> indicates a client error.
     */
    public final boolean isClientError() {
        return ResponseCode.isClientError(code);
    }

    /**
     * Checks whether this response's code indicates a server error.
     *
     * @return {@code true} if <em>code</em> indicates a server error.
     */
    public final boolean isServerError() {
        return ResponseCode.isServerError(code);
    }
}
