package io.zbejas.ping.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Configuration class for the Ping plugin.
 * Contains all configurable pingSettings for the plugin behavior.
 */
public class PingConfig {

    /**
     * Nested configuration class for ping effect pingSettings.
     */
    public static class PingSettings {
        public static final BuilderCodec<PingSettings> CODEC = BuilderCodec
                .builder(PingSettings.class, PingSettings::new)
                .append(new KeyedCodec<>("PlayerEyeHeightOffset", Codec.DOUBLE),
                        (pingSettings, value) -> pingSettings.playerEyeHeightOffset = value,
                        pingSettings -> pingSettings.playerEyeHeightOffset)
                .add()
                .append(new KeyedCodec<>("HeightOffset", Codec.DOUBLE),
                        (pingSettings, value) -> pingSettings.heightOffset = value,
                        pingSettings -> pingSettings.heightOffset)
                .add()
                .append(new KeyedCodec<>("HorizontalOffset", Codec.DOUBLE),
                        (pingSettings, value) -> pingSettings.horizontalOffset = value,
                        pingSettings -> pingSettings.horizontalOffset)
                .add()
                .append(new KeyedCodec<>("DistanceOffset", Codec.DOUBLE),
                        (pingSettings, value) -> pingSettings.distanceOffset = value,
                        pingSettings -> pingSettings.distanceOffset)
                .add()
                .append(new KeyedCodec<>("ParticleEffect", Codec.STRING),
                        (pingSettings, value) -> pingSettings.particleEffect = value,
                        pingSettings -> pingSettings.particleEffect)
                .add()
                .append(new KeyedCodec<>("SoundEvent", Codec.STRING),
                        (pingSettings, value) -> pingSettings.soundEvent = value,
                        pingSettings -> pingSettings.soundEvent)
                .add()
                .append(new KeyedCodec<>("DurationSeconds", Codec.INTEGER),
                        (pingSettings, value) -> pingSettings.durationSeconds = value,
                        pingSettings -> pingSettings.durationSeconds)
                .add()
                .append(new KeyedCodec<>("IntervalMs", Codec.LONG),
                        (pingSettings, value) -> pingSettings.intervalMs = value,
                        pingSettings -> pingSettings.intervalMs)
                .add()
                .append(new KeyedCodec<>("DebugParticleEffect", Codec.STRING),
                        (pingSettings, value) -> pingSettings.debugParticleEffect = value,
                        pingSettings -> pingSettings.debugParticleEffect)
                .add()
                .append(new KeyedCodec<>("MaxDistance", Codec.INTEGER),
                        (pingSettings, value) -> pingSettings.maxDistance = value,
                        pingSettings -> pingSettings.maxDistance)
                .add()
                .build();

        public double playerEyeHeightOffset = 1.6;
        public double heightOffset = 1.0;
        public double horizontalOffset = 0.5;
        public double distanceOffset = 1.0;
        public String particleEffect = "Shield_Block";
        public String soundEvent = "SFX_Crystal_Break";
        public int durationSeconds = 10;
        public long intervalMs = 500;
        public String debugParticleEffect = "BeamEmiter_Heal_Red";
        public int maxDistance = 500;
    }

    /**
     * Codec for serializing and deserializing the ping configuration.
     */
    public static final BuilderCodec<PingConfig> CODEC = BuilderCodec.builder(PingConfig.class, PingConfig::new)
            .append(new KeyedCodec<>("DebugMode", Codec.BOOLEAN),
                    (pingConfig, debugMode) -> pingConfig.debugMode = debugMode,
                    PingConfig::isDebugMode)
            .add()
            .append(new KeyedCodec<>("PingSettings", PingSettings.CODEC),
                    (pingConfig, pingSettings) -> pingConfig.pingSettings = pingSettings,
                    PingConfig::getPingSettings)
            .add()
            .build();

    private boolean debugMode = false;
    private PingSettings pingSettings = new PingSettings();

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Gets the ping pingSettings configuration.
     *
     * @return the ping pingSettings
     */
    public PingSettings getPingSettings() {
        return pingSettings;
    }
}
