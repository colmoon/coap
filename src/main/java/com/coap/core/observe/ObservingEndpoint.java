package com.coap.core.observe;

/**
 * @ClassName ObservingEndpoint
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/14 21:54
 * @Version 1.0
 **/

import com.coap.core.coap.Token;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents an observing endpoint. It holds all observe relations
 * that the endpoint has to this server. If a confirmable notification timeouts
 * for the maximum times allowed the server assumes the client is no longer
 * reachable and cancels all relations that it has established to resources.
 */

public class ObservingEndpoint {

    /** The endpoint's address */
    private final InetSocketAddress address;

    /** The list of relations the endpoint has established with this server */
    private final List<ObserveRelation> relations;

    /**
     * Constructs a new ObservingEndpoint.
     * @param address the endpoint's address
     */
    public ObservingEndpoint(InetSocketAddress address) {
        this.address = address;
        this.relations = new CopyOnWriteArrayList<ObserveRelation>();
    }

    /**
     * Adds the specified observe relation.
     * @param relation the relation
     */
    public void addObserveRelation(ObserveRelation relation) {
        relations.add(relation);
    }

    /**
     * Removes the specified observe relations.
     * @param relation the relation
     */
    public void removeObserveRelation(ObserveRelation relation) {
        relations.remove(relation);
    }

    /**
     * Cancels all observe relations that this endpoint has established with
     * resources from this server.
     */
    public void cancelAll() {
        for (ObserveRelation relation : relations) {
            relation.cancel();
        }
    }

    /**
     * Returns the address of this endpoint-
     * @return the address
     */
    public InetSocketAddress getAddress() {
        return address;
    }

    public ObserveRelation getObserveRelation(Token token) {
        if (token != null) {
            for (ObserveRelation relation:relations) {
                if (token.equals(relation.getExchange().getRequest().getToken())) {
                    return relation;
                }
            }
        }
        return null;
    }
}
