package com.coap.core;


/**
 * An execution environment for CoAP {@link Resource}s.
 *
 * A server hosts a tree of {@link Resource}s which are exposed to clients by
 * means of one or more {@link Endpoint}s which are bound to a network interface.
 *
 * A server can be started and stopped. When the server stops the endpoint
 * frees the port it is listening on, but keeps the executors running to resume.
 * <p>
 * The following code snippet provides an example of a server with a resource
 * that responds with a <em>"hello world"</em> to any incoming GET request.
 * <pre>
 *   CoapServer server = new CoapServer(port);
 *   server.add(new CoapResource(&quot;hello-world&quot;) {
 * 	   public void handleGET(CoapExchange exchange) {
 * 	  	 exchange.respond(ResponseCode.CONTENT, &quot;hello world&quot;);
 * 	   }
 *   });
 *   server.start();
 * </pre>
 *
 * The following figure shows the server's basic architecture.
 *
 * <pre>
 * +------------------------------------- CoapServer --------------------------------------+
 * |                                                                                       |
 * |                               +-----------------------+                               |
 * |                               |    MessageDeliverer   +--&gt; (Resource Tree)            |
 * |                               +---------A-A-A---------+                               |
 * |                                         | | |                                         |
 * |                                         | | |                                         |
 * |                 .--------&gt;&gt;&gt;------------' | '--------&lt;&lt;&lt;------------.                 |
 * |                /                          |                          \                |
 * |               |                           |                           |               |
 * |             * A                         * A                         * A               |
 * | +-----------------------+   +-----------------------+   +-----------------------+     |
 * | |        Endpoint       |   |        Endpoint       |   |      Endpoint         |     |
 * | +-----------------------+   +-----------------------+   +-----------------------+     |
 * +------------v-A--------------------------v-A-------------------------v-A---------------+
 *              v A                          v A                         v A
 *              v A                          v A                         v A
 *           (Network)                    (Network)                   (Network)
 * </pre>
 *
 * @see MessageDeliverer
 * @see Endpoint
 **/

import com.coap.core.network.config.NetworkConfig;
import com.coap.core.server.ServerInterface;
import com.coap.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName CoapServer
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/15 9:41
 * @Version 1.0
 **/

public class CoapServer implements ServerInterface {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CoapServer.class.getName());

    /** The root resource. */
    private final Resource root;

    /** The network configuration used by this server. */
    private final NetworkConfig config;

    /** The message deliverer. */
    private MessageDeliverer deliverer;



}
