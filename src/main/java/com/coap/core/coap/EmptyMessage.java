package com.coap.core.coap;

import static com.coap.core.coap.CoAP.*;

/**
 * @ClassName EmptyMessage
 * @Description EmptyMessage
 * @Author wuxiaojian
 * @Date 2019/6/17 14:23
 * @Version 1.0
 **/

/**
 * EmptyMessage represents an empty CoAP message. An empty message has either
 * the message {@link Type} ACK or RST.
 */
public class EmptyMessage extends Message {

    /**
     * Instantiates a new empty message.
     *
     * @param type the message type (ACK or RST)
     */
    public EmptyMessage(Type type) {
        super(type);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String appendix = "";
        // crude way to check nothing extra is set in an empty message
        if (!hasEmptyToken() || getOptions().asSortedList().size() > 0 || getPayloadSize() > 0) {
            String payload = getPayloadString();
            if (payload == null) {
                payload = "no payload";
            } else {
                int len = payload.length();
                if (payload.indexOf("\n") != -1) {
                    payload = payload.substring(0, payload.indexOf("\n"));
                }
                if (payload.length() > 24) {
                    payload = payload.substring(0, 20);
                }
                payload = "\"" + payload + "\"";
                if (payload.length() != len + 2) {
                    payload += ".. " + payload.length() + " bytes";
                }
            }
            appendix = " NON-EMPTY: Token=" + getTokenString() + ", " + getOptions() + ", " + payload;
        }
        return String.format("%s        MID=%5d%s", getType(), getMID(), appendix);
    }

    @Override
    public int getRawCode() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * EMPTY messages are never intended to have payload!
     */
    @Override
    public boolean isIntendedPayload() {
        return false;
    }

    /**
     * Create a new acknowledgment for the specified message.
     *
     * @param message the message to acknowledge
     * @return the acknowledgment
     */
    public static EmptyMessage newACK(Message message) {
        EmptyMessage ack = new EmptyMessage(Type.ACK);
        ack.setDestinationContext(message.getSourceContext());
        ack.setMID(message.getMID());
        return ack;
    }

    /**
     * Create a new reset message for the specified message.
     *
     * @param message the message to reject
     * @return the reset
     */
    public static EmptyMessage newRST(Message message) {
        EmptyMessage rst = new EmptyMessage(Type.RST);
        rst.setDestinationContext(message.getSourceContext());
        rst.setMID(message.getMID());
        return rst;
    }

}

