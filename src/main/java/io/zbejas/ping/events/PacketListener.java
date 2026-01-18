package io.zbejas.ping.events;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import io.zbejas.ping.service.PingService;

import java.util.UUID;

/**
 * Packet listener that intercepts interaction packets and delegates them to the
 * PingService.
 * Specifically handles SyncInteractionChains packets (ID 290) to enable ping
 * functionality.
 */
public class PacketListener implements PacketWatcher {
    private final PingService pingService;

    /**
     * Constructs a new PacketListener.
     *
     * @param pingService the ping service to delegate interactions to
     */
    public PacketListener(PingService pingService) {
        this.pingService = pingService;
    }

    /**
     * Processes incoming packets, filtering for SyncInteractionChains and
     * delegating to PingService.
     *
     * @param packetHandler the packet handler containing authentication information
     * @param packet        the incoming packet to process
     */
    @Override
    public void accept(PacketHandler packetHandler, Packet packet) {
        if (packet.getId() != 290) {
            return;
        }

        PlayerAuthentication playerAuth = packetHandler.getAuth();
        if (playerAuth == null) {
            return;
        }

        UUID uuid = playerAuth.getUuid();
        if (uuid == null) {
            return;
        }

        PlayerRef player = Universe.get().getPlayer(uuid);
        if (player == null) {
            return;
        }

        SyncInteractionChains interactionChains = (SyncInteractionChains) packet;
        pingService.processInteractionChains(player, interactionChains);
    }
}
