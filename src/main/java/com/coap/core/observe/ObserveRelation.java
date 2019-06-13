package com.coap.core.observe;

/**
 * @ClassName ObserveRelation
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/13 21:48
 * @Version 1.0
 **/

import com.coap.core.coap.Response;
import com.coap.core.network.Exchange;
import com.coap.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The ObserveRelation is a server-side control structure. It represents a
 * relation between a client endpoint and a resource on this server.
 */
public class ObserveRelation {

    /** The logger. */
    private final static Logger LOGGER = LoggerFactory.getLogger(ObserveRelation.class.getCanonicalName());

    private final long checkIntervalTime;
    private final int checkIntervalCount;

    private final ObservingEndpoint endpoint;

    /** The resource that is observed */
    private final Resource resource;

    /** The exchange that has established the observe relationship */
    private final Exchange exchange;

    private Response recentControlNotification;
    private Response nextControlNotification;

    private String key = null;

    /*
     * This value is false at first and must be set to true by the resource if
     * it accepts the observe relation (the response code must be successful).
     */
    /** Indicates if the relation is established */
    private volatile boolean established;
    /** Indicates if the relation is canceled */
    private volatile boolean canceled;




}
