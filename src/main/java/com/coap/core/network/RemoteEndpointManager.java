package com.coap.core.network;

import com.coap.core.network.config.NetworkConfig;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public class RemoteEndpointManager {

	// Maximum amount of destinations for which remote endpoint objects are maintained
	private final int MAX_REMOTE_ENDPOINTS = 10;
	
	/** The list of remote endpoints */
	private LimitedRemoteEndpointHashmap<InetAddress,RemoteEndpoint> remoteEndpointsList = new LimitedRemoteEndpointHashmap<InetAddress,RemoteEndpoint>(MAX_REMOTE_ENDPOINTS);//ArrayList<RemoteEndpoint>(0);

	/** The configuration */ 
	private NetworkConfig config;
	
	/**
	 * The RemoteEndpointManager is responsible for creating a new RemoteEndpoint object when exchanges with a 
	 * new destination endpoint are initiated and managing existing ones.
	 * 
	 * @param config the network parameter configuration
	 */
	public RemoteEndpointManager(NetworkConfig config) {
		this.config = config;
	}
		
	/**
	 * Returns the endpoint responsible for the given exchange.
	 * @param exchange the exchange
	 * @return the endpoint for the exchange
	 */
	public RemoteEndpoint getRemoteEndpoint(Exchange exchange){ //int remotePort, InetAddress remoteAddress){

		InetSocketAddress remoteSocketAddress = exchange.getRequest().getDestinationContext().getPeerAddress();
		InetAddress remoteAddress = remoteSocketAddress.getAddress();
		int remotePort = remoteSocketAddress.getPort();

		// TODO: One IP-Address is considered to be a destination endpoint, for higher granularity (portnumber) changes are necessary
		if (!remoteEndpointsList.containsKey(remoteAddress)){
			RemoteEndpoint unusedRemoteEndpoint = new RemoteEndpoint(remotePort, remoteAddress, config);
			remoteEndpointsList.put(remoteAddress,unusedRemoteEndpoint);
			
			//System.out.println("Number of RemoteEndpoint objects stored:" + remoteEndpointsList.size());
		}

		return remoteEndpointsList.get(remoteAddress);
	}
	
	public class LimitedRemoteEndpointHashmap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -7855412701242966797L;
		private final int maxSize;

	    public LimitedRemoteEndpointHashmap(int maxSize) {
	        this.maxSize = maxSize;
	    }

	    @Override
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	        return size() > maxSize;
	    }
	}
}
