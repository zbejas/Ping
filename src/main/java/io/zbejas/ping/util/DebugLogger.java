package io.zbejas.ping.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.zbejas.ping.config.ConfigManager;

import java.util.logging.Level;

/**
 * Utility logger that respects the debug mode configuration.
 * Provides methods for logging at various levels and sending debug messages to
 * players.
 */
public class DebugLogger {
    private final HytaleLogger logger;
    private final String name;

    /**
     * Constructs a new DebugLogger with the specified name.
     *
     * @param name the name to prefix log messages with
     */
    public DebugLogger(String name) {
        this.logger = HytaleLogger.getLogger();
        this.name = name;
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message the message to log
     */
    public void debug(String message) {
        if (ConfigManager.isDebugMode()) {
            logger.at(Level.FINE).log("[" + name + "] " + message);
        }
    }

    /**
     * Sends a debug message to a specific player if debug mode is enabled.
     *
     * @param player  the player to send the message to
     * @param message the message to send
     */
    public void debugPlayer(PlayerRef player, String message) {
        if (ConfigManager.isDebugMode()) {
            player.sendMessage(Message.raw(message));
        }
    }

    /**
     * Logs an info-level message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        logger.at(Level.INFO).log(message);
    }

    /**
     * Logs a warning-level message.
     *
     * @param message the message to log
     */
    public void warning(String message) {
        logger.at(Level.WARNING).log("[" + name + "] " + message);
    }

    /**
     * Logs an error-level message.
     *
     * @param message the message to log
     */
    public void error(String message) {
        logger.at(Level.SEVERE).log("[" + name + "] " + message);
    }
}
