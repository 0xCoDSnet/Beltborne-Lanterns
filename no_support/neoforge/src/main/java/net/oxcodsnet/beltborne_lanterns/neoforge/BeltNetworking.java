package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

import java.util.UUID;

/**
 * NeoForge networking helpers for S2C belt state sync.
 */
public final class BeltNetworking {
    private BeltNetworking() {}

    public static void broadcastBeltState(ServerPlayerEntity subject, Item lamp) {
        BeltSyncPayload payload = new BeltSyncPayload(subject.getUuid(), lamp != null ? LampRegistry.getId(lamp) : null);
        PacketDistributor.sendToPlayersTrackingEntity(subject, payload);
        PacketDistributor.sendToPlayer(subject, payload);
    }

    public static void sendTo(ServerPlayerEntity target, UUID subjectUuid, Item lamp) {
        PacketDistributor.sendToPlayer(target, new BeltSyncPayload(subjectUuid, lamp != null ? LampRegistry.getId(lamp) : null));
    }
}

