package com.coap.core.coap;

import com.coap.elements.util.Bytes;

/**
 * @ClassName Token
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/14 21:58
 * @Version 1.0
 **/

/**
 * Implementation of CoAP token.
 */
public class Token extends Bytes {

    /**
     * Empty token.
     */
    public static final Token EMPTY = new Token(Bytes.EMPTY);

    /**
     * Create token from bytes.
     *
     * @param token token bytes to be copied
     */
    public Token(byte[] token) {
        this(token, true);
    }

    /**
     * Create token from bytes.
     *
     * @param token token bytes
     * @param copy {@code true}, to copy the bytes, {@code false}, to use the
     *            bytes without copy
     * @throws NullPointerException if token is {@code null}
     * @throws IllegalArgumentException if tokens length is larger than 8 (as
     *             specified in CoAP)
     */
    private Token(byte[] token, boolean copy) {
        super(token, 8, copy);
    }

    @Override
    public String toString() {
        return new StringBuilder("Token=").append(getAsString()).toString();
    }

    /**
     * Create token from provider token.
     *
     * Doesn't copy the provided token.
     *
     * @param token token, not copied!
     * @return created Token
     */
    public static Token fromProvider(byte[] token) {
        return new Token(token, false);
    }

}
