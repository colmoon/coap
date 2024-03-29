package com.coap.elements.util;

/**
 * @ClassName Bytes
 * @Description Bytes
 * @Author wuxiaojian
 * @Date 2019/6/14 21:59
 * @Version 1.0
 **/

import java.util.Arrays;
import java.util.Random;

/**
 * Byte array as key.
 */
public class Bytes {
    /**
     * Empty byte array.
     */
    public static final byte[] EMPTY = new byte[0];
    /**
     * bytes.
     */
    private final byte[] bytes;
    /**
     * Pre-calculated hash.
     *
     * @see #hashCode()
     */
    private final int hash;
    /**
     * Create bytes array.
     *
     * @param bytes bytes (not copied!)
     * @throws NullPointerException if bytes is {@code null}
     * @throws IllegalArgumentException if bytes length is larger than 255
     */
    public Bytes(byte[] bytes) {
        this(bytes, 255, false);
    }

    /**
     * Create bytes array.
     *
     * @param bytes bytes
     * @param maxLength maximum length of bytes
     * @param copy {@code true} to copy bytes, {@code false} to use the provided bytes
     * @throws NullPointerException if bytes is {@code null}
     * @throws IllegalArgumentException if bytes length is larger than maxLength
     */
    public Bytes(byte[] bytes, int maxLength, boolean copy) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        } else if (bytes.length > maxLength) {
            throw new IllegalArgumentException("bytes length must be between 0 and " + maxLength + " inclusive");
        }
        this.bytes = copy ? Arrays.copyOf(bytes,  bytes.length) : bytes;
        this.hash = Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return new StringBuilder("BYTES=").append(getAsString()).toString();
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bytes other = (Bytes) obj;
        if (hash != other.hash)
            return false;
        return Arrays.equals(bytes, other.bytes);
    }

    /**
     * Get bytes array.
     *
     * @return bytes array. Not Copied!
     */
    public final byte[] getBytes() {
        return bytes;
    }

    /**
     * Get bytes as (hexadecimal) string.
     *
     * @return bytes as (hexadecimal) string
     */
    public final String getAsString() {
        return StringUtil.byteArray2Hex(bytes);
    }

    /**
     * Check, if connection id is empty.
     *
     * @return {@code true}, if connection id is empty, {@code false}, otherwise
     */
    public final boolean isEmpty() {
        return bytes.length == 0;
    }

    /**
     * Return number of bytes.
     *
     * @return number of bytes. 0 to 255.
     */
    public final int length() {
        return bytes.length;
    }

    /**
     * Create byte array initialized with random bytes.
     *
     * @param generator random generator
     * @param size number of bytes
     * @return byte array initialized with random bytes
     * @see Random#nextBytes(byte[])
     */
    public static byte[] createBytes(Random generator, int size) {
        byte[] byteArray = new byte[size];
        generator.nextBytes(byteArray);
        return byteArray;
    }
}
