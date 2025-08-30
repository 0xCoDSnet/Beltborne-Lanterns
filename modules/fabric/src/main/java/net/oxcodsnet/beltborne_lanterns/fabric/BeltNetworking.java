package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

import java.util.UUID;

public final class BeltNetworking {
    private BeltNetworking() {}

    public static void broadcastBeltState(ServerPlayerEntity subject, boolean hasLantern) {
        // Send to watchers + the subject themselves
        for (ServerPlayerEntity target : PlayerLookup.tracking(subject)) {
            ServerPlayNetworking.send(target, new BeltSyncPayload(subject.getUuid(), hasLantern));
        }
        ServerPlayNetworking.send(subject, new BeltSyncPayload(subject.getUuid(), hasLantern));
    }

    public static void sendTo(ServerPlayerEntity target, UUID subjectUuid, boolean hasLantern) {
        ServerPlayNetworking.send(target, new BeltSyncPayload(subjectUuid, hasLantern));
    }
}
