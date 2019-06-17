package com.coap.core;

/**
 * @ClassName Utils
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/17 14:04
 * @Version 1.0
 **/

import com.coap.core.coap.MediaTypeRegistry;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;
import com.coap.elements.util.StringUtil;

/**
 * Auxiliary(辅助的) helper methods for Californium.
 */
public class Utils {
    /*
     * Prevent initialization
     */
    private Utils() {
        // nothing to do
    }

    /**
     * Converts the specified byte array to a hexadecimal string.
     *
     * @param bytes the byte array
     * @return the hexadecimal code string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes == null) {
            sb.append("null");
        } else {
            sb.append("[");
            for(byte b : bytes) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Converts the specified byte array up to the specified length into a hexadecimal text.
     * Separate bytes by spaces and group them in lines. Append length of array, if specified
     * length is smaller then the length of the array.
     *
     * @param bytes the array of bytes. If null, the text "null" is returned.
     * @param length length up to the bytes should be converted into hexadecimal text.
     *               If larger then the array length, reduce it to the array length.
     * @return byte array as hexadecimal text
     */
    public static String toHexText(byte[] bytes, int length) {
        if (bytes == null) return "null";
        if (length > bytes.length) length = bytes.length;
        StringBuilder sb = new StringBuilder();
        if (16 < length) sb.append(StringUtil.lineSeparator());
        for(int index = 0; index < length; ++index) {
            sb.append(String.format("%02x", bytes[index] & 0xFF));
            if (31 == (31 & index)) {
                sb.append(StringUtil.lineSeparator());
            } else {
                sb.append(' ');
            }
        }
        if (length < bytes.length) {
            sb.append(" .. ").append(bytes.length).append(" bytes");
        }
        return sb.toString();
    }

    /**
     * Formats a {@link Request} into a readable String representation.
     *
     * @param r the Request
     * @return the pretty print
     */
    public static String prettyPrint(Request r) {

        StringBuilder sb = new StringBuilder();

        sb.append("==[ CoAP Request ]=============================================").append(StringUtil.lineSeparator());
        sb.append(String.format("MID    : %d", r.getMID())).append(StringUtil.lineSeparator());
        sb.append(String.format("Token  : %s", r.getTokenString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Type   : %s", r.getType().toString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Method : %s", r.getCode().toString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Options: %s", r.getOptions().toString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Payload: %d Bytes", r.getPayloadSize())).append(StringUtil.lineSeparator());
        if (r.getPayloadSize() > 0 && MediaTypeRegistry.isPrintable(r.getOptions().getContentFormat())) {
            sb.append("---------------------------------------------------------------").append(StringUtil.lineSeparator());
            sb.append(r.getPayloadString());
            sb.append(StringUtil.lineSeparator());
        }
        sb.append("===============================================================");

        return sb.toString();
    }

    /**
     * Formats a {@link CoapResponse} into a readable String representation.
     *
     * @param r the CoapResponse
     * @return the pretty print
     */
    public static String prettyPrint(CoapResponse r) {
        return prettyPrint(r.advanced());
    }

    /**
     * Formats a {@link Response} into a readable String representation.
     *
     * @param r the Response
     * @return the pretty print
     */
    public static String prettyPrint(Response r) {
        StringBuilder sb = new StringBuilder();

        sb.append("==[ CoAP Response ]============================================").append(StringUtil.lineSeparator());
        sb.append(String.format("MID    : %d", r.getMID())).append(StringUtil.lineSeparator());
        sb.append(String.format("Token  : %s", r.getTokenString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Type   : %s", r.getType().toString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Status : %s", r.getCode().toString())).append(StringUtil.lineSeparator());
        sb.append(String.format("Options: %s", r.getOptions().toString())).append(StringUtil.lineSeparator());
        if (r.getRTT() != null) {
            sb.append(String.format("RTT    : %d ms", r.getRTT())).append(StringUtil.lineSeparator());
        }
        sb.append(String.format("Payload: %d Bytes", r.getPayloadSize())).append(StringUtil.lineSeparator());
        if (r.getPayloadSize() > 0 && MediaTypeRegistry.isPrintable(r.getOptions().getContentFormat())) {
            sb.append("---------------------------------------------------------------").append(StringUtil.lineSeparator());
            sb.append(r.getPayloadString());
            sb.append(StringUtil.lineSeparator());
        }
        sb.append("===============================================================");

        return sb.toString();
    }
}
