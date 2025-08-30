package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ExampleModFabricClient implements ClientModInitializer {
    // Client-only cache of players who currently have a belt lantern
    private static final Set<UUID> CLIENT_BELT_PLAYERS = new HashSet<>();

    public static boolean clientHasLantern(PlayerEntity player) {
        return CLIENT_BELT_PLAYERS.contains(player.getUuid());
    }

    @Override
    public void onInitializeClient() {
        // Register network receiver: updates local client set
        ClientPlayNetworking.registerGlobalReceiver(BeltSyncPayload.ID, (payload, context) -> {
            UUID uuid = payload.playerUuid();
            boolean has = payload.hasLantern();
            MinecraftClient.getInstance().execute(() -> {
                if (has) CLIENT_BELT_PLAYERS.add(uuid); else CLIENT_BELT_PLAYERS.remove(uuid);
            });
        });

        // Register a feature renderer for players to draw the lantern on the belt
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (entityType == EntityType.PLAYER) {
                helper.register(new LanternBeltFeatureRenderer(renderer));
            }
        });
    }
}
