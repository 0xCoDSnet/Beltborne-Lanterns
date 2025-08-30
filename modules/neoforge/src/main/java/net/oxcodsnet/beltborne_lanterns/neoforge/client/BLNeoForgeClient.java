package net.oxcodsnet.beltborne_lanterns.neoforge.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternBeltFeatureRenderer;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BLNeoForgeClient {
    private static final Set<UUID> CLIENT_BELT_PLAYERS = new HashSet<>();

    private BLNeoForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Provide platform bridges for common renderer
        BLClientAbstractions.init(BLNeoForgeClient::clientHasLantern, () -> false);
    }

    private static boolean clientHasLantern(PlayerEntity p) {
        return CLIENT_BELT_PLAYERS.contains(p.getUuid());
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        // Add our belt lantern feature to player renderers (both skins)
        PlayerEntityRenderer defaultSkin = event.getSkin("default");
        PlayerEntityRenderer slimSkin = event.getSkin("slim");
        if (defaultSkin != null) defaultSkin.addFeature(new LanternBeltFeatureRenderer<>(defaultSkin));
        if (slimSkin != null) slimSkin.addFeature(new LanternBeltFeatureRenderer<>(slimSkin));
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Register S2C payload for syncing which players have a belt lantern
        var registrar = event.registrar(BLMod.MOD_ID);
        registrar.playToClient(
                BeltSyncPayload.ID,
                BeltSyncPayload.CODEC,
                (payload, ctx) -> ctx.workHandler().submit(() -> {
                    UUID uuid = payload.playerUuid();
                    boolean has = payload.hasLantern();
                    if (has) CLIENT_BELT_PLAYERS.add(uuid); else CLIENT_BELT_PLAYERS.remove(uuid);
                })
        );
    }

    @EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT)
    public static final class ClientBus {
        private ClientBus() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post e) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return;
            float dt = 1f / 20f;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (clientHasLantern(p)) {
                    LanternSwingManager.tickPlayer(p, dt, BLConfigs.get().rotXDeg);
                }
            }
        }
    }
}

