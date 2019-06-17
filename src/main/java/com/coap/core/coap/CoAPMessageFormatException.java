package com.coap.core.coap;

/**
 * @ClassName CoAPMessageFormatException
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/17 14:22
 * @Version 1.0
 **/

/**
 * Indicates a problem while parsing the binary representation of a CoAP
 * message.
 * <p>
 * The <em>message</em> property contains a description of the problem
 * encountered. The other properties are parsed from the binary representation.
 * </p>
 */
public class CoAPMessageFormatException extends MessageFormatException {

    private static final long serialVersionUID = 1L;
    private static final int NO_MID = -1;
    private final int mid;
    private final int code;
    private final boolean confirmable;

    /**
     * Creates an exception for a description and message properties.
     *
     * @param description a description of the error cause.
     * @param mid the message ID.
     * @param code the message code.
     * @param confirmable whether the message has been transferred reliably.
     */
    public CoAPMessageFormatException(String description, int mid, int code, boolean confirmable) {
        super(description);
        this.mid = mid;
        this.code = code;
        this.confirmable = confirmable;
    }

    /**
     * Checks if the message's ID could be parsed successfully.
     *
     * @return {@code true} if the value returned by <em>getMid</em> is the real
     *         message ID.
     */
    public final boolean hasMid() {
        return mid > NO_MID;
    }

    /**
     * @return the mid
     */
    public final int getMid() {
        return mid;
    }

    /**
     * @return the code
     */
    public final int getCode() {
        return code;
    }

    /**
     * Checks if the message has been transferred reliably.
     *
     * @return {@code true} if the message type is CON.
     */
    public final boolean isConfirmable() {
        return confirmable;
    }
}

