package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

import java.util.UUID;

public final class BeltNetworking {
    private BeltNetworking() {}

    public static void broadcastBeltState(ServerPlayerEntity subject, Item lamp) {
        // Send to watchers + the subject themselves
        for (ServerPlayerEntity target : PlayerLookup.tracking(subject)) {
            var buf = PacketByteBufs.create();
            new BeltSyncPayload(subject.getUuid(), lamp != null ? LampRegistry.getId(lamp) : null).write(buf);
            ServerPlayNetworking.send(target, BeltSyncPayload.ID, buf);
        }
        var selfBuf = PacketByteBufs.create();
        new BeltSyncPayload(subject.getUuid(), lamp != null ? LampRegistry.getId(lamp) : null).write(selfBuf);
        ServerPlayNetworking.send(subject, BeltSyncPayload.ID, selfBuf);
    }

    public static void sendTo(ServerPlayerEntity target, UUID subjectUuid, Item lamp) {
        var buf = PacketByteBufs.create();
        new BeltSyncPayload(subjectUuid, lamp != null ? LampRegistry.getId(lamp) : null).write(buf);
        ServerPlayNetworking.send(target, BeltSyncPayload.ID, buf);
    }
}
