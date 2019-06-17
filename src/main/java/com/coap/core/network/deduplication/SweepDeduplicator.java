package com.coap.core.network.deduplication;

/**
 * @ClassName SweepDeduplicator
 * @Description SweepDeduplicator
 * @Author wuxiaojian
 * @Date 2019/6/13 22:06
 * @Version 1.0
 **/

import com.coap.core.network.Exchange;
import com.coap.core.network.Exchange.KeyMID;
import com.coap.core.network.config.NetworkConfig;
import com.coap.elements.util.ClockUtil;
import com.coap.elements.util.ExecutorsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This deduplicator uses an in-memory map to store incoming messages.
 * <p>
 * The deduplicator periodically iterates through all entries and removes
 * messages (exchanges) that have been received before EXCHANGE_LIFETIME seconds.
 * </p>
 */
public final class SweepDeduplicator implements Deduplicator {

    private final static Logger LOGGER = LoggerFactory.getLogger(SweepDeduplicator.class.getName());

    /**
     * Add timestamp for deduplication to Exchange.
     */
    private static class DedupExchange {

        /**
         * Nano-timestamp for deduplication of Exchange.
         */
        public final long nanoTimestamp;
        /**
         * Exchange to be deduplicated.
         */
        public final Exchange exchange;

        /**
         * Create new exchange for deduplication.
         *
         * @param exchange Exchange to be deduplicated
         */
        public DedupExchange(Exchange exchange) {
            this.exchange = exchange;
            this.nanoTimestamp = ClockUtil.nanoRealtime();
        }
    }

    /** The hash map with all incoming messages. */
    private final ConcurrentMap<KeyMID, DedupExchange> incomingMessages = new ConcurrentHashMap<>();
    private final SweepAlgorithm algorithm;
    private final long sweepInterval;
    private final long exchangeLifetime;
    private volatile ScheduledFuture<?> jobStatus;

    /**
     * Creates a new deduplicator from configuration values.
     * <p>
     * The following configuration values are used to initialize the
     * sweep algorithm used by this deduplicator:
     * <ul>
     * <li>{@link com.coap.core.network.config.NetworkConfig.Keys#EXCHANGE_LIFETIME} -
     * an exchange is removed from this deduplicator if no messages have been received for this number
     * of milliseconds</li>
     * <li>{@link com.coap.core.network.config.NetworkConfig.Keys#MARK_AND_SWEEP_INTERVAL} -
     * the interval at which to check for expired exchanges in milliseconds</li>
     * </ul>
     *
     * @param config the configuration to use.
     */
    public SweepDeduplicator(final NetworkConfig config) {
        algorithm = new SweepAlgorithm();
        sweepInterval = config.getLong(NetworkConfig.Keys.MARK_AND_SWEEP_INTERVAL);
        exchangeLifetime = config.getLong(NetworkConfig.Keys.EXCHANGE_LIFETIME);
    }

    @Override
    public synchronized void start() {
        if (jobStatus == null) {
            jobStatus = ExecutorsUtil.getScheduledExecutor().scheduleAtFixedRate(algorithm, sweepInterval, sweepInterval,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public synchronized void stop() {
        if (jobStatus != null) {
            jobStatus.cancel(false);
            jobStatus = null;
            clear();
        }
    }

    /**
     * If the message with the specified {@link KeyMID} has already arrived
     * before, this method returns the corresponding exchange. If this
     * KeyMID has not yet arrived, this method returns null, indicating that
     * the message with the KeyMID is not a duplicate. In this case, the
     * exchange is added to the deduplicator.
     */
    @Override
    public Exchange findPrevious(final KeyMID key, final Exchange exchange) {
        DedupExchange previous = incomingMessages.putIfAbsent(key, new DedupExchange(exchange));
        return null == previous ? null : previous.exchange;
    }

    @Override
    public Exchange find(KeyMID key) {
        DedupExchange previous = incomingMessages.get(key);
        return null == previous ? null : previous.exchange;
    }

    @Override
    public void clear() {
        incomingMessages.clear();
    }

    @Override
    public boolean isEmpty() {
        return incomingMessages.isEmpty();
    }

    @Override
    public int size() {
        return incomingMessages.size();
    }

    /**
     * The sweep algorithm periodically iterates over all exchanges and removes
     * obsolete entries.
     */
    private class SweepAlgorithm implements Runnable {

        /**
         * This method wraps the method sweep() to catch any Exceptions that
         * might be thrown.
         */
        @Override
        public void run() {
            try {
                LOGGER.trace("Start Mark-And-Sweep with {} entries", incomingMessages.size());
                sweep();

            } catch (Throwable t) {
                LOGGER.warn("Exception in Mark-and-Sweep algorithm", t);
            }
        }

        /**
         * Iterate through all entries and remove the obsolete ones.
         */
        private void sweep() {

            if (!incomingMessages.isEmpty()) {
                final long start = ClockUtil.nanoRealtime();
                final long oldestAllowed = start - TimeUnit.MILLISECONDS.toNanos(exchangeLifetime);

                // Notice that ConcurrentHashMap guarantees the correctness for this iteration.
                for (Map.Entry<?, DedupExchange> entry : incomingMessages.entrySet()) {
                    DedupExchange exchange = entry.getValue();
                    if ((exchange.nanoTimestamp - oldestAllowed) < 0) {
                        //TODO check if exchange of observe relationship is periodically created and sweeped
                        LOGGER.trace("Mark-And-Sweep removes {}", entry.getKey());
                        incomingMessages.remove(entry.getKey());
                    }
                }
                LOGGER.debug("Sweep run took {}ms", TimeUnit.NANOSECONDS.toMillis(ClockUtil.nanoRealtime() - start));
            }
        }
    }
}
