package com.coap.core.network.stack.congestioncontrol;

import com.coap.core.network.Exchange;
import com.coap.core.network.RemoteEndpoint;
import com.coap.core.network.config.NetworkConfig;

public class CocoaStrong extends Cocoa{

	public CocoaStrong(NetworkConfig config) {
		super(config);
		setDithering(true);
	}
	
	@Override
	public void processRTTmeasurement(long measuredRTT, Exchange exchange, int retransmissionCount){
		//System.out.println("Measured an RTT of " + measuredRTT + " after using " + retransmissionCount + " retries." );	
		RemoteEndpoint endpoint = getRemoteEndpoint(exchange);
		int rtoType = endpoint.getExchangeEstimatorState(exchange);				
		
		if(rtoType == NOESTIMATOR || rtoType == WEAKRTOTYPE)
			return;
		endpoint.matchCurrentRTO();
	
		//System.out.println("Measured RTT:" + measuredRTT);
		
		// System.out.println("Endpoint status: blindweak/blindstrong/state : " + endpoint.isBlindWeak() + "/" + endpoint.isBlindStrong() + "/" + endpoint.getExchangeEstimatorState(exchange));
		if (endpoint.isBlindStrong() && rtoType == STRONGRTOTYPE) {
			// Received a strong RTT measurement for the first time, apply
			// strong RTO update
			endpoint.setBlindStrong(false);
			initializeRTOEstimators(measuredRTT, STRONGRTOTYPE, endpoint);
		} else {
			// Perform normal update of the RTO
			updateEstimator(measuredRTT, rtoType, endpoint);
		}
	}
}
