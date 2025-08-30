package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.fabric.BeltNetworking;

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
        ClientPlayNetworking.registerGlobalReceiver(BeltNetworking.CHANNEL, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            boolean has = buf.readBoolean();
            client.execute(() -> {
                if (has) CLIENT_BELT_PLAYERS.add(uuid); else CLIENT_BELT_PLAYERS.remove(uuid);
            });
        });

        // Register a feature renderer for players to draw the lantern on the belt
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, context) -> {
            if (entityType == EntityType.PLAYER) {
                renderer.addFeature(new LanternBeltFeatureRenderer(renderer, context.getItemRenderer()));
            }
        });
    }
}
