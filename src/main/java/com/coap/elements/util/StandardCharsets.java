package com.coap.elements.util;

import java.nio.charset.Charset;

/**
 * @ClassName StandardCharsets
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/15 16:24
 * @Version 1.0
 **/

public interface StandardCharsets {
    Charset UTF_8 = Charset.forName("UTF-8");
    Charset US_ASCII = Charset.forName("US-ASCII");
    Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
}