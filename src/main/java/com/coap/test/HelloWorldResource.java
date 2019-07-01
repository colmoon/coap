package com.coap.test;


/**
 * @ClassName HelloWorldResource
 * @Description
 * @Author wu.xiao.jian
 * @Version V1.0.0
 * @Date 2018/11/16 13:25
 */

import com.coap.core.CoapResource;
import com.coap.core.server.resources.CoapExchange;

/**
 * This resource responds with a kind "hello world" to GET requests.
 */
public class HelloWorldResource extends CoapResource {

    public HelloWorldResource(String name) {
        super(name);
        //添加子url
        add(new HelloWorldResource());
    }

    public HelloWorldResource() {
        super("on");
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        String ip=exchange.getSourceAddress().getHostAddress();
        String port=String.valueOf(exchange.getSourcePort());
        String address=ip+":"+port;
        System.out.println(address);
        String hex11 = exchange.getRequestText();
        exchange.respond("hello world");
    }

}