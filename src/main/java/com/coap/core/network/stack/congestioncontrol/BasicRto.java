package com.coap.core.network.stack.congestioncontrol;

import com.coap.core.network.Exchange;
import com.coap.core.network.RemoteEndpoint;
import com.coap.core.network.config.NetworkConfig;
import com.coap.core.network.stack.CongestionControlLayer;

/**
 * @ClassName BasicRto
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/13 22:11
 * @Version 1.0
 **/

public class BasicRto extends CongestionControlLayer {

    public BasicRto(NetworkConfig config) {
        super(config);
    }

    @Override
    protected void updateEstimator(long measuredRTT, int estimatorType, RemoteEndpoint endpoint){
        // Use last RTT measurement, which is then multiplied by a static factor (dithering)
        long newRTO =  measuredRTT; //; (long) (measuredRTT * 1.5);
        //System.out.println("Basic RTO: " + measuredRTT );

        endpoint.updateRTO(newRTO);
    }

    @Override
    public void processRTTmeasurement(long measuredRTT, Exchange exchange, int retransmissionCount){
        //System.out.println("Measured an RTT of " + measuredRTT + " after using " + retransmissionCount + " retries." );
        RemoteEndpoint endpoint = getRemoteEndpoint(exchange);
        int rtoType = endpoint.getExchangeEstimatorState(exchange);

        // The basic rto algorithm does not care for the blind estimator, set weak/strong to false
        endpoint.setBlindStrong(false);
        endpoint.setBlindWeak(false);
        //Perform normal update of the RTO
        updateEstimator(measuredRTT, rtoType, endpoint);

    }
}
