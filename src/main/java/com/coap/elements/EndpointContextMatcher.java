package com.coap.elements;

/**
 * Interface for endpoint context processing.
 * 
 * Enable implementor to flexible decide on endpoint context information.
 */
public interface EndpointContextMatcher {

	/**
	 * Return matcher name. Used for logging.
	 * 
	 * @return name of strategy.
	 */
	String getName();

	/**
	 * Check, if responses is related to the request.
	 * 
	 * @param requestContext endpoint context of request
	 * @param responseContext endpoint context of response
	 * @return true, if response is related to the request, false, if response
	 *         should not be considered for this request.
	 */
	boolean isResponseRelatedToRequest(EndpointContext requestContext, EndpointContext responseContext);

	/**
	 * Check, if message should be sent out using the current endpoint
	 * context of the connector.
	 * 
	 * @param messageContext endpoint context of message
	 * @param connectionContext endpoint context of connection
	 * @return true, if message should be sent, false, if message should not be
	 *         sent.
	 */
	boolean isToBeSent(EndpointContext messageContext, EndpointContext connectionContext);

}
