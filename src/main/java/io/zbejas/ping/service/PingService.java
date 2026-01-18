package io.zbejas.ping.service;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.zbejas.ping.config.ConfigManager;
import io.zbejas.ping.config.PingConfig.PingSettings;
import io.zbejas.ping.util.DebugLogger;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for handling ping interactions in the game world.
 * Allows players to mark locations by creating visual and audio effects at
 * targeted positions.
 */
public class PingService {

    private static final DebugLogger logger = new DebugLogger("PingService");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final double RAYCAST_STEP_SIZE = 1.0;
    private static final int PARTICLE_SPAWN_COUNT = 5;
    public PingSettings pingSettings = ConfigManager.getPingSettings();

    /**
     * Processes interaction chains from a player to detect ping requests.
     *
     * @param player The player performing the interaction
     * @param packet The interaction chains packet containing interaction data
     */
    public void processInteractionChains(PlayerRef player, SyncInteractionChains packet) {
        if (player == null || packet == null) {
            return;
        }

        for (SyncInteractionChain chain : packet.updates) {
            logger.debugPlayer(player, "Interaction Type: " + chain.interactionType);

            if (chain.interactionType == InteractionType.Pick) {
                handlePickInteraction(player, chain);
                break;
            }
        }
    }

    /**
     * Handles a pick interaction from the player.
     * Validates the interaction and initiates ping creation on the world thread.
     *
     * @param player the player who performed the interaction
     * @param chain  the interaction chain containing interaction details
     */
    private void handlePickInteraction(PlayerRef player, SyncInteractionChain chain) {
        logger.debugPlayer(player, "Pick detected!");

        if (isBlockClick(chain)) {
            logger.debugPlayer(player, "Clicked on a block, ignoring...");
            return;
        }

        logger.debugPlayer(player, "No block position in interaction data, using raycast...");

        World world = getPlayerWorld(player);
        if (world == null) {
            return;
        }

        world.execute(() -> handlePickInteractionOnWorldThread(player, world));
    }

    /**
     * Processes the ping creation on the world thread.
     * Performs raycasting to find the target location and spawns the ping effect.
     *
     * @param player the player creating the ping
     * @param world  the world in which to create the ping
     */
    private void handlePickInteractionOnWorldThread(PlayerRef player, World world) {
        Transform transform;
        try {
            transform = player.getTransform();
        } catch (Exception e) {
            logger.debug("Failed to get player transform: " + e.getMessage());
            return;
        }

        if (transform == null) {
            logger.debugPlayer(player, "Could not get player transform");
            return;
        }

        Vector3d eyePosition = calculateEyePosition(transform.getPosition());
        Vector3d lookDirection = getLookDirection(player, transform);

        logger.debug(String.format("Player position: %.2f, %.2f, %.2f",
                transform.getPosition().x, transform.getPosition().y, transform.getPosition().z));

        BlockPosition targetBlock = raycastToBlock(player, eyePosition, lookDirection, world);

        if (targetBlock != null) {
            Vector3d pingLocation = calculatePingLocation(targetBlock, lookDirection);
            spawnPingEffect(player, pingLocation);

            logger.debugPlayer(player, String.format("Ping created at: %.2f, %.2f, %.2f",
                    pingLocation.x, pingLocation.y, pingLocation.z));
        } else {
            logger.debugPlayer(player, "No block in sight (looking at sky/void)");
        }
    }

    /**
     * Performs a raycast from the origin in the given direction to find the first
     * solid block.
     *
     * @param player    the player performing the raycast (for debug messages)
     * @param origin    the starting position of the ray
     * @param direction the direction vector of the ray
     * @param world     the world to raycast in
     * @return the position of the hit block, or null if no block was found
     */
    private BlockPosition raycastToBlock(PlayerRef player, Vector3d origin, Vector3d direction, World world) {
        boolean debugMode = ConfigManager.isDebugMode();

        for (int i = 0; i < pingSettings.maxDistance; i++) {
            Vector3d rayPoint = calculateRayPoint(origin, direction, i);
            BlockPosition blockPos = toBlockPosition(rayPoint);

            if (debugMode) {
                spawnDebugParticle(world, rayPoint);
            }

            if (isBlockSolid(world, blockPos)) {
                return blockPos;
            }
        }

        return null;
    }

    /**
     * Spawns a repeating ping effect at the specified location.
     * The effect includes particles and sound, repeating at the configured
     * interval.
     *
     * @param player   the player who created the ping
     * @param location the location at which to spawn the ping effect
     */
    private void spawnPingEffect(PlayerRef player, Vector3d location) {
        World world = getPlayerWorld(player);
        if (world == null) {
            player.sendMessage(Message.raw("Error: Could not find world."));
            return;
        }

        AtomicInteger spawnCount = new AtomicInteger(0);
        int durationSeconds = pingSettings.durationSeconds;
        long intervalMs = pingSettings.intervalMs;

        // Calculate total number of spawns based on duration and interval
        int maxSpawns = (int) ((durationSeconds * 1000) / intervalMs);

        scheduler.scheduleAtFixedRate(() -> {
            int currentCount = spawnCount.incrementAndGet();

            if (currentCount > maxSpawns) {
                return;
            }

            world.execute(() -> spawnSinglePingEffect(player, location, world));
        }, 0, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Spawns a single instance of the ping effect (particles + sound).
     *
     * @param player   the player who created the ping
     * @param location the location at which to spawn the effect
     * @param world    the world in which to spawn the effect
     */
    private void spawnSinglePingEffect(PlayerRef player, Vector3d location, World world) {
        try {
            Store<EntityStore> store = world.getEntityStore().getStore();

            spawnParticles(location, store);
            playSound(location, store);
        } catch (Exception e) {
            player.sendMessage(Message.raw("Error creating ping effect: " + e.getMessage()));
            logger.debug("Ping effect error: " + e.getMessage());
        }
    }

    /**
     * Checks if the interaction chain represents a click on a block.
     *
     * @param chain the interaction chain to check
     * @return true if the interaction is a block click, false otherwise
     */
    private boolean isBlockClick(SyncInteractionChain chain) {
        return chain.data != null && chain.data.blockPosition != null;
    }

    /**
     * Checks if a block at the given position is solid.
     *
     * @param world the world containing the block
     * @param pos   the position of the block to check
     * @return true if the block is solid, false if it is empty
     */
    private boolean isBlockSolid(World world, BlockPosition pos) {
        return world.getBlock(pos.x, pos.y, pos.z) != BlockType.EMPTY_ID;
    }

    /**
     * Retrieves the world that the player is currently in.
     *
     * @param player the player whose world to retrieve
     * @return the player's world, or null if it cannot be retrieved
     */
    private World getPlayerWorld(PlayerRef player) {
        UUID worldUuid = player.getWorldUuid();
        if (worldUuid == null) {
            logger.debugPlayer(player, "Could not get world UUID");
            return null;
        }

        World world = Universe.get().getWorld(worldUuid);
        if (world == null) {
            logger.debugPlayer(player, "Could not get world");
        }
        return world;
    }

    /**
     * Calculates the eye position of the player by adding eye height to their
     * position.
     *
     * @param playerPosition the player's current position
     * @return the eye position
     */
    private Vector3d calculateEyePosition(Vector3d playerPosition) {
        return new Vector3d(
                playerPosition.x,
                playerPosition.y + pingSettings.playerEyeHeightOffset,
                playerPosition.z);
    }

    /**
     * Gets the direction the player is looking.
     * Attempts to use HeadRotation component, falls back to body direction.
     *
     * @param player    the player whose look direction to retrieve
     * @param transform the player's transform
     * @return the look direction vector
     */
    @SuppressWarnings("deprecation")
    private Vector3d getLookDirection(PlayerRef player, Transform transform) {
        HeadRotation headRotation = player.getComponent(HeadRotation.getComponentType());

        if (headRotation != null) {
            Vector3d direction = headRotation.getDirection();
            logger.debug(String.format("Using HeadRotation direction: %.4f, %.4f, %.4f",
                    direction.x, direction.y, direction.z));
            return direction;
        }

        Vector3d direction = transform.getDirection();
        logger.debug(String.format("HeadRotation not found, using body direction: %.4f, %.4f, %.4f",
                direction.x, direction.y, direction.z));
        return direction;
    }

    /**
     * Calculates a point along the ray at a given step distance.
     *
     * @param origin    the origin of the ray
     * @param direction the direction of the ray
     * @param step      the step number along the ray
     * @return the calculated point
     */
    private Vector3d calculateRayPoint(Vector3d origin, Vector3d direction, int step) {
        double t = step * RAYCAST_STEP_SIZE;
        return new Vector3d(
                origin.x + direction.x * t,
                origin.y + direction.y * t,
                origin.z + direction.z * t);
    }

    /**
     * Converts a 3D position vector to a block position by flooring coordinates.
     *
     * @param position the position to convert
     * @return the block position
     */
    private BlockPosition toBlockPosition(Vector3d position) {
        return new BlockPosition(
                (int) Math.floor(position.x),
                (int) Math.floor(position.y),
                (int) Math.floor(position.z));
    }

    /**
     * Calculates the final ping location based on the target block and look
     * direction.
     * Positions the ping slightly offset from the block surface.
     *
     * @param targetBlock   the block that was targeted
     * @param lookDirection the direction the player was looking
     * @return the calculated ping location
     */
    private Vector3d calculatePingLocation(BlockPosition targetBlock, Vector3d lookDirection) {
        double horizontalOffset = pingSettings.horizontalOffset;
        double distanceOffset = pingSettings.distanceOffset;
        double heightOffset = pingSettings.heightOffset;

        Vector3d closerPosition = new Vector3d(
                targetBlock.x + horizontalOffset - lookDirection.x * distanceOffset,
                targetBlock.y + horizontalOffset - lookDirection.y * distanceOffset,
                targetBlock.z + horizontalOffset - lookDirection.z * distanceOffset);

        int blockX = (int) Math.floor(closerPosition.x);
        int blockY = (int) Math.floor(closerPosition.y);
        int blockZ = (int) Math.floor(closerPosition.z);

        return new Vector3d(
                blockX + horizontalOffset,
                blockY - horizontalOffset + heightOffset,
                blockZ + horizontalOffset);
    }

    /**
     * Spawns particle effects at the specified location.
     *
     * @param location the location at which to spawn particles
     * @param store    the entity store for spawning particles
     */
    private void spawnParticles(Vector3d location, Store<EntityStore> store) {
        String particleEffect = pingSettings.particleEffect;
        for (int i = 0; i < PARTICLE_SPAWN_COUNT; i++) {
            ParticleUtil.spawnParticleEffect(particleEffect, location, store);
        }
    }

    /**
     * Plays a sound effect at the specified location.
     *
     * @param location the location at which to play the sound
     * @param store    the entity store for playing sounds
     */
    private void playSound(Vector3d location, Store<EntityStore> store) {
        String soundEvent = pingSettings.soundEvent;
        SoundUtil.playSoundEvent3d(
                SoundEvent.getAssetMap().getIndex(soundEvent),
                SoundCategory.SFX,
                location.x,
                location.y,
                location.z,
                store);
    }

    /**
     * Spawns a debug particle for visualizing the raycast path.
     *
     * @param world    the world in which to spawn the particle
     * @param position the position at which to spawn the particle
     */
    private void spawnDebugParticle(World world, Vector3d position) {
        String debugParticle = pingSettings.debugParticleEffect;
        world.execute(() -> {
            ParticleUtil.spawnParticleEffect(
                    debugParticle,
                    position,
                    world.getEntityStore().getStore());
        });
    }
}
