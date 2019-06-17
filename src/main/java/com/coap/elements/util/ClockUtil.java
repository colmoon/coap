package com.coap.elements.util;

/**
 * Clock utility. Provides a {@linkplain ClockUtil#handler handler}.
 * 
 * Intended to be used for system as android, where {@link System#nanoTime()}
 * reflects the system uptime without system sleep times. Android devices, which
 * want to use californium timer's (e.g. auto resumption or deduplication) in
 * sleep times, must set their handler and provide the proper expired
 * nanoseconds. This will not wake up any californium timer in time, but it
 * expires them on then next wake up. Not used for schedule timed of schedule
 * timer e.g. {@link ExecutorsUtil}.
 */
public class ClockUtil {

	/**
	 * Handler to return the expired realtime in nanoseconds.
	 */
	public interface Realtime {

		/**
		 * Get the system specific expired realtime in nanoseconds.
		 * 
		 * @return expired realtime in nanoseconds.
		 */
		long nanoRealtime();
	}

	/**
	 * Handler for system specific expired realtime in nanoseconds. Default
	 * calls {@link System#nanoTime()}. Overridden calling
	 * {@link #setRealtimeHandler(Realtime)} with a system specific
	 * implementation.
	 */
	private static volatile Realtime handler = new Realtime() {

		@Override
		public long nanoRealtime() {
			return System.nanoTime();
		}
	};

	/**
	 * Set handler system specific expired realtime in nanoseconds.
	 * 
	 * @param systemHandler system specific expired realtime in nanoseconds.
	 * @throws NullPointerException if systemHandler is {@code null}
	 */
	public static void setRealtimeHandler(Realtime systemHandler) {
		if (systemHandler == null) {
			throw new NullPointerException("realtime system handler must not be null!");
		}
		handler = systemHandler;
	}

	/**
	 * Get expired realtime in nanoseconds.
	 * 
	 * If no system specific handler was set before, the default handler calling
	 * {@link System#nanoTime()} is used.
	 * 
	 * @return expired realtime in nanoseconds
	 */
	public static long nanoRealtime() {
		return handler.nanoRealtime();
	}
}
