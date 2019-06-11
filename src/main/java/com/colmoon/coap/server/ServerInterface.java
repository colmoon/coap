package com.colmoon.coap.server;

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



}
