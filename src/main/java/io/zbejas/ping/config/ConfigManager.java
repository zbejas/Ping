package io.zbejas.ping.config;

import com.hypixel.hytale.server.core.util.Config;

/**
 * Manages access to the plugin configuration.
 * Provides a centralized way to retrieve configuration values throughout the
 * plugin.
 */
public class ConfigManager {
    private static Config<PingConfig> config;

    /**
     * Initializes the configuration manager with the provided config instance.
     *
     * @param cfg the configuration instance to manage
     */
    public static void initialize(Config<PingConfig> cfg) {
        config = cfg;
    }

    /**
     * Retrieves the current ping configuration.
     *
     * @return the ping configuration instance
     * @throws IllegalStateException if the configuration has not been initialized
     */
    public static PingConfig get() {
        if (config == null) {
            throw new IllegalStateException("Config not initialized");
        }
        return config.get();
    }

    /**
     * Checks if debug mode is enabled in the configuration.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    public static boolean isDebugMode() {
        return get().isDebugMode();
    }

    /**
     * Retrieves the ping settings configuration.
     *
     * @return the ping settings
     */
    public static PingConfig.PingSettings getPingSettings() {
        return get().getPingSettings();
    }
}
