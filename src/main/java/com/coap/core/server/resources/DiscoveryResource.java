package com.coap.core.server.resources;

import com.coap.core.CoapResource;
import com.coap.core.coap.CoAP;
import com.coap.core.coap.CoAP.*;
import com.coap.core.coap.LinkFormat;
import com.coap.core.coap.MediaTypeRegistry;

import java.util.List;

/**
 * The DiscoveryResource implements CoAP's discovery service. It is typically
 * accessible over CoAP on the well-known URI: <tt>/.well-known/core</tt>. It
 * responds to GET requests with a list of the server's resources, i.e. links.
 */
public class DiscoveryResource extends CoapResource {

	/** The Constant CORE. */
	public static final String CORE = "core";
	
	/** The root of the server's resource tree */
	private final Resource root;
	
	/**
	 * Instantiates a new discovery resource.
	 *
	 * @param root the root resource of the server
	 */
	public DiscoveryResource(Resource root) {
		this(CORE, root);
	}
	
	/**
	 * Instantiates a new discovery resource with the specified name.
	 *
	 * @param name the name
	 * @param root the root resource of the server
	 */
	public DiscoveryResource(String name, Resource root) {
		super(name);
		this.root = root;
	}
	
	/**
	 * Responds with a list of all resources of the server, i.e. links.
	 * 
	 * @param exchange the exchange
	 */
	@Override
	public void handleGET(CoapExchange exchange) {
		List<String> query = exchange.getRequestOptions().getUriQuery();
		if (query.size() <= 1) {
			String tree = discoverTree(root, query);
			exchange.respond(CoAP.ResponseCode.CONTENT, tree, MediaTypeRegistry.APPLICATION_LINK_FORMAT);
		}
		else {
			exchange.respond(ResponseCode.BAD_OPTION, "only one search query is supported!", MediaTypeRegistry.TEXT_PLAIN);
		}
	}
	
	/**
	 * Builds up the list of resources of the specified root resource. Queries
	 * serve as filter and might prevent undesired resources from appearing on
	 * the list.
	 * 
	 * @param root the root resource of the server
	 * @param queries the queries
	 * @return the list of resources as string
	 */
	public String discoverTree(Resource root, List<String> queries) {
		StringBuilder buffer = new StringBuilder();
		for (Resource child : root.getChildren()) {
			LinkFormat.serializeTree(child, queries, buffer);
		}
		
		// remove last comma ',' of the buffer
		if (buffer.length() > 1) {
			buffer.setLength(buffer.length() - 1);
		}
		
		return buffer.toString();
	}
}
