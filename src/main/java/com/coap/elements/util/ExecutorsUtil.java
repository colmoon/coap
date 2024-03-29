package com.coap.elements.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility to create executors.
 * 
 * Note: THE INTERNAL/PRIVATE {@code SplitScheduledThreadPoolExecutor} IS A
 * WORKAROUND! IT MAY BE REPLACED IN THE FUTURE. See issue #690.
 */
public class ExecutorsUtil {

	/** the logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorsUtil.class.getCanonicalName());

	private static final Runnable WARMUP = new Runnable() {

		@Override
		public void run() {
			LOGGER.trace("warmup ...");
		}
	};

	/**
	 * Thread group for timers.
	 */
	public static final ThreadGroup TIMER_THREAD_GROUP = new ThreadGroup("Timer"); //$NON-NLS-1$

	/**
	 * General scheduled executor intended for rare executing timers (e.g.
	 * cleanup task).
	 */
	private static final ScheduledThreadPoolExecutor scheduler;

	static {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2,
				new DaemonThreadFactory("Timer#", TIMER_THREAD_GROUP));
		executor.execute(WARMUP);
		executor.prestartAllCoreThreads();
		scheduler = executor;
	}

	/**
	 * Threshold for using the {@link SplitScheduledThreadPoolExecutor} to split
	 * thread pool into scheduled and immediately executing threads. {@code 0}
	 * to disable the use of the {@link SplitScheduledThreadPoolExecutor}.
	 */
	private static final int SPLIT_THRESHOLD = 1;

	/**
	 * Create a scheduled thread pool executor service.
	 * 
	 * If the provided number of threads exceeds the {@link #SPLIT_THRESHOLD},
	 * the {@code SplitScheduledThreadPoolExecutor} is returned.
	 * 
	 * @param poolSize number of threads for thread pool.
	 * @param threadFactory thread factory
	 * @return thread pool based scheduled executor service
	 */
	public static ScheduledExecutorService newScheduledThreadPool(int poolSize, ThreadFactory threadFactory) {
		if (SPLIT_THRESHOLD == 0 || poolSize <= SPLIT_THRESHOLD) {
			LOGGER.trace("create scheduled thread pool of {} threads", poolSize);
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(poolSize, threadFactory);
			executor.execute(WARMUP);
			return executor;
		} else {
			LOGGER.trace("create special thread pool of {} threads", poolSize);
			SplitScheduledThreadPoolExecutor executor = new SplitScheduledThreadPoolExecutor(poolSize, threadFactory);
			executor.execute(WARMUP);
			executor.schedule(WARMUP, 0, TimeUnit.NANOSECONDS);
			return executor;
		}
	}

	/**
	 * Create a fixed thread pool.
	 * 
	 * @param poolSize number of threads for thread pool.
	 * @param threadFactory thread factory
	 * @return thread pool based executor service
	 */
	public static ExecutorService newFixedThreadPool(int poolSize, ThreadFactory threadFactory) {
		LOGGER.trace("create thread pool of {} threads", poolSize);
		ExecutorService executor = Executors.newFixedThreadPool(poolSize, threadFactory);
		executor.execute(WARMUP);
		return executor;
	}

	/**
	 * Create a single threaded scheduled executor service.
	 * 
	 * @param threadFactory thread factory
	 * @return single threaded scheduled executor service
	 */
	public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
		LOGGER.trace("create scheduled single thread pool");
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
		executor.execute(WARMUP);
		return executor;
	}

	/**
	 * Get general scheduled executor.
	 * 
	 * Intended to be used for rare executing timers (e.g. cleanup tasks).
	 * 
	 * @return scheduled executor service
	 */
	public static ScheduledThreadPoolExecutor getScheduledExecutor() {
		return scheduler;
	}

	/**
	 * Executor, which uses a {@link ScheduledThreadPoolExecutor}
	 * with {@link ExecutorsUtil#SPLIT_THRESHOLD} threads for scheduling, and an
	 * additional {@link ThreadPoolExecutor} for execution. This may result in
	 * better performance for direct job, when many scheduled job are queued for
	 * execution.
	 * 
	 * Note: IT'S A WORKAROUND! IT MAY BE REPLACED IN THE FUTURE! See issue #690.
	 */
	private static class SplitScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

		private static final long QUEUE_SIZE_DIFF = 20000;
		/**
		 * Direct thread pool executor for direct execution.
		 */
		private final ExecutorService directExecutor;
		/**
		 * Last schedule queue size.
		 */
		private AtomicLong scheduleQueueSize = new AtomicLong();

		/**
		 * Create new executor.
		 * 
		 * Split thread pool in {@link ScheduledThreadPoolExecutor} with
		 * {@link ExecutorsUtil#SPLIT_THRESHOLD} threads a
		 * {@link ThreadPoolExecutor} with the left number of threads of the
		 * provide pool size.
		 * 
		 * @param corePoolSize total number of threads used for this executor.
		 * @param threadFactory thread factory.
		 */
		public SplitScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
			super(corePoolSize < SPLIT_THRESHOLD ? corePoolSize : SPLIT_THRESHOLD, threadFactory);
			setMaximumPoolSize(corePoolSize < SPLIT_THRESHOLD ? corePoolSize : SPLIT_THRESHOLD);
			if (corePoolSize > SPLIT_THRESHOLD) {
				directExecutor = newFixedThreadPool(corePoolSize - SPLIT_THRESHOLD, threadFactory);
			} else {
				directExecutor = null;
			}
		}

		@Override
		public void execute(Runnable command) {
			if (directExecutor == null) {
				super.execute(command);
			} else {
				long lastSize = scheduleQueueSize.get();
				long size = getQueue().size();
				long diff = Math.abs(lastSize - size);
				if (diff > QUEUE_SIZE_DIFF && scheduleQueueSize.compareAndSet(lastSize, size)) {
					LOGGER.debug("Job queue {}", size);
				}
				directExecutor.execute(command);
			}
		}

		@Override
		public Future<?> submit(Runnable task) {
			if (directExecutor == null) {
				return super.submit(task);
			} else {
				return directExecutor.submit(task);
			}
		}

		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			if (directExecutor == null) {
				return super.submit(task, result);
			} else {
				return directExecutor.submit(task, result);
			}
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			if (directExecutor == null) {
				return super.submit(task);
			} else {
				return directExecutor.submit(task);
			}
		}

		@Override
		public void shutdown() {
			if (directExecutor != null) {
				directExecutor.shutdown();
			}
			super.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			List<Runnable> result = super.shutdownNow();
			if (directExecutor != null) {
				result.addAll(directExecutor.shutdownNow());
			}
			return result;
		}
	}
}
