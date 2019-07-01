package com.coap.test;


import com.coap.core.*;
import com.coap.core.coap.Request;
import com.coap.core.coap.Response;

import java.util.concurrent.Executors;

/**
 * @ClassName ExampleServer
 * @Description
 * @Author wu.xiao.jian
 * @Version V1.0.0
 * @Date 2018/11/16 13:19
 */
public class ExampleServer {

    public static void main(String[] args) throws Exception {
        CoapServer server = new CoapServer();
        server.setExecutor(Executors.newScheduledThreadPool(4));

        server.add(new HelloWorldResource("hello"));

        server.start();

        selfTest();

        clientTest();

//        byte[] bytes = new byte[]{49,50,51,52,53};
//        System.out.println(Utils.toHexString(bytes));
//        System.out.println(Utils.toHexText(bytes,5));
    }

    /*
     *  Sends a GET request to itself
     */
    public static void selfTest() {
        try {
            Request request = Request.newGet();
            request.setURI("localhost:5683/hello/on");
//            request.setURI("localhost:5683/.well-known/core");
            request.send();
            Response response = request.waitForResponse(1000);
            System.out.println("received "+response);
            System.out.println("received "+response.getPayloadString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clientTest(){
        CoapClient coapClient = new CoapClient();
        coapClient.setURI("localhost:5683/hello");
        coapClient.setTimeout(1000L);
        coapClient.get(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                System.out.println(response.getCode());  //打印请求状态码
                System.out.println(response.getOptions());  //选项参数
                System.out.println(response.getResponseText());  //获取内容文本信息
                System.out.println("\nAdvanced\n");    //
                System.out.println(Utils.prettyPrint(response));  //打印格式良好的输出
            }

            @Override
            public void onError() {
                System.out.println("error");
            }
        });
    }
}
