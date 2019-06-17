package com.coap.core.observe;

import com.coap.core.coap.Request;

/**
 * Utility for observation.
 */
public final class ObservationUtil {

	/**
	 * Create shallow clone of observation and the contained request.
	 * 
	 * @return a cloned observation with a shallow clone of request, or null, if
	 *         null was provided.
	 * @throws IllegalArgumentException, if observation didn't contain a
	 *             request.
	 */
	public static Observation shallowClone(Observation observation) {
		if (null == observation) {
			return null;
		}
		Request request = observation.getRequest();
		if (null == request) {
			throw new IllegalArgumentException("missing request for observation!");
		}
		Request clonedRequest = new Request(request.getCode());
		clonedRequest.setDestinationContext(request.getDestinationContext());
		clonedRequest.setType(request.getType());
		clonedRequest.setMID(request.getMID());
		clonedRequest.setToken(request.getToken());
		clonedRequest.setOptions(request.getOptions());
		clonedRequest.setPayload(request.getPayload());
		clonedRequest.setUserContext(request.getUserContext());
		return new Observation(clonedRequest, observation.getContext());
	}
}
