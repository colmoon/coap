package com.coap.core.coap;

/**
 * @ClassName MessageFormatException
 * @Description MessageFormatException
 * @Author wuxiaojian
 * @Date 2019/6/15 16:20
 * @Version 1.0
 **/
/**
 * Indicates a problem while parsing the binary representation of a message.
 * <p>
 * The <em>message</em> property contains a description of the problem
 * encountered.
 * </p>
 */
public class MessageFormatException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception for a description.
     *
     * @param description a description of the error cause.
     */
    public MessageFormatException(final String description) {
        super(description);
    }
}
