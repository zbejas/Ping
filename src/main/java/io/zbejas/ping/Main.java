package io.zbejas.ping;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import io.zbejas.ping.config.ConfigManager;
import io.zbejas.ping.config.PingConfig;
import io.zbejas.ping.events.PacketListener;
import io.zbejas.ping.service.PingService;
import io.zbejas.ping.util.DebugLogger;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * Main plugin class for the Ping plugin.
 * Initializes configuration and registers packet listeners for ping
 * functionality.
 */
public class Main extends JavaPlugin {
    private static final DebugLogger logger = new DebugLogger("Main");
    private final Config<PingConfig> config;

    /**
     * Constructs the Main plugin instance.
     *
     * @param init the plugin initialization context
     */
    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("PingConfig", PingConfig.CODEC);
    }

    /**
     * Sets up the plugin by initializing configuration, services, and packet
     * listeners.
     */
    @Override
    protected void setup() {
        this.config.save();
        ConfigManager.initialize(this.config);

        PingService pingService = new PingService();
        PacketAdapters.registerInbound(new PacketListener(pingService));

        logger.info("Ping Plugin enabled!");
    }
}