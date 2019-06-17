package com.coap.core.observe;

import com.coap.core.coap.Request;
import com.coap.core.coap.Response;

/**
 * Client code can register a notification listener on an {@code Endpoint} in
 * order to be called back when notifications for observed resources are
 * received from peers.
 * <p>
 * Notification listeners are registered at a <em>global</em> level only, i.e.
 * the listener will be invoked for all notifications for all observed
 * resources. This is in contrast to the {@code CoapHandler} that client code
 * can register when invoking one of {@code CoapClient}'s methods and which is
 * called back for notifications for a particular observed resource only.
 * </p>
 */
public interface NotificationListener {

	/**
	 * Invoked when a notification for an observed resource has been received.
	 * 
	 * @param request
	 *            The original request that was used to establish the
	 *            observation.
	 * @param response
	 *            the notification.
	 */
	void onNotification(Request request, Response response);
}
