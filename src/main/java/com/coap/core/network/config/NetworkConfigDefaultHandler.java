package com.coap.core.network.config;

/**
 * @ClassName NetworkConfigDefaultHandler
 * @Description NetworkConfigDefaultHandler
 * @Author wuxiaojian
 * @Date 2019/6/17 14:28
 * @Version 1.0
 **/

/**
 * Handler for custom setup of default network configuration. Called after
 * {@link NetworkConfigDefaults#setDefaults(NetworkConfig)}.
 */
public interface NetworkConfigDefaultHandler {

    /**
     * Apply custom defaults.
     *
     * @param config network configuration to be filled with custom defaults.
     */
    void applyDefaults(NetworkConfig config);
}
