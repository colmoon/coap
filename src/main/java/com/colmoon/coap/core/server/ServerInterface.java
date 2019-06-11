package com.colmoon.coap.core.server;

import com.colmoon.coap.core.network.Endpoint;
import com.colmoon.coap.core.server.resources.Resource;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @ClassName ServerInterface
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/11 22:52
 * @Version 1.0
 **/

public interface ServerInterface {
    // be a server

    /**
     * Starts the server by starting all endpoints this server is assigned to.
     * Each endpoint binds to its port. If no endpoint is assigned to the
     * server, the server binds to CoAP's default port 5683.
     *
     * Implementations should start all registered endpoints as part of this method.
     * @throws IllegalStateException if the server could not be started properly,
     * e.g. because none of its endpoints could be bound to their respective
     * ports
     */
    void start();

    /**
     * Stops the server, i.e. unbinds it from all ports.
     *
     * Frees as much system resources as possible while still being able to
     * be started again.
     * Implementations should stop all registered endpoints as part of this method.
     */
    void stop();

    /**
     * Destroys the server, i.e. unbinds from all ports and frees all system
     * resources.
     *
     * The server instance is not expected to be able to be started again once
     * this method has been invoked.
     */
    void destroy();

    /**
     * Adds one or more resources to the server.
     *
     * @param resources the resources
     * @return the server
     */
    ServerInterface add(Resource... resources);

    /**
     * Removes a resource from the server.
     *
     * @param resource the resource to be removed
     * @return <code>true</code> if the resource has been removed successfully
     */
    boolean remove(Resource resource);

    /**
     * Adds an endpoint for receive and sending CoAP messages on.
     *
     * @param endpoint the endpoint
     * @throws NullPointerException if the endpoint is <code>null</code>
     */
    void addEndpoint(Endpoint endPoint);

    /**
     * Gets the endpoints this server is bound to.
     *
     * @return the endpoints
     */
    List<Endpoint> getEndpoints();

    /**
     * Gets the endpoint bound to a particular address.
     *
     * @param address the address
     * @return the endpoint or <code>null</code> if none of the
     * server's endpoints is bound to the given address
     */
    Endpoint getEndpoint(InetSocketAddress address);

    /**
     * Gets an endpoint bound to a particular port.
     *
     * If the server has multiple endpoints on different network interfaces
     * bound to the same port, an implementation may return any of those endpoints.
     *
     * @param port the port
     * @return the endpoint or <code>null</code> if none of the
     * server's endpoints is bound to the given port on any of its
     * network interfaces
     */
    Endpoint getEndpoint(int port);



}
