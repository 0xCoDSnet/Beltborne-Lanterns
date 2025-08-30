package net.oxcodsnet.beltborne_lanterns.neoforge.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.client.ui.LanternDebugScreen;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternBeltFeatureRenderer;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.client.ClientBeltPlayers;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientLogic;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientScreens;

import java.util.UUID;

@EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BLNeoForgeClient {
    // no per-loader state; use common ClientBeltPlayers

    private BLNeoForgeClient() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Provide platform bridges for common renderer
        BLClientAbstractions.init(ClientBeltPlayers::hasLantern, BLClientAbstractions::isDebugDrawEnabled);
        LambDynLightsCompat.init();

        // Register the config screen with NeoForge's extension point
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, parent) -> LanternClientScreens.openConfig(parent)
        );
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        // Add our belt lantern feature to all available player skins
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof PlayerEntityRenderer per) {
                per.addFeature(new LanternBeltFeatureRenderer<>(per));
            }
        }
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Register S2C payload for syncing which players have a belt lantern
        var registrar = event.registrar("1"); // network version
        registrar.playToClient(
                BeltSyncPayload.ID,
                BeltSyncPayload.CODEC,
                (payload, ctx) -> {
                    UUID uuid = payload.playerUuid();
                    boolean has = payload.hasLantern();
                    ClientBeltPlayers.setHas(uuid, has);
                }
        );
    }

    @EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT)
    public static final class ClientBus {
        private ClientBus() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post e) {
            MinecraftClient mc = MinecraftClient.getInstance();
            // Hotkeys (without formal key mapping): L to open config, K toggle debug, P open editor
            if (mc.currentScreen == null && mc.getWindow() != null) {
                long win = mc.getWindow().getHandle();
                // GLFW constants: 76=L, 75=K, 80=P
                if (org.lwjgl.glfw.GLFW.glfwGetKey(win, org.lwjgl.glfw.GLFW.GLFW_KEY_L) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    mc.setScreen(me.shedaniel.autoconfig.AutoConfig.getConfigScreen(BLClientConfig.class, mc.currentScreen).get());
                } else if (org.lwjgl.glfw.GLFW.glfwGetKey(win, org.lwjgl.glfw.GLFW.GLFW_KEY_K) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    BLClientAbstractions.setDebugDrawEnabled(!BLClientAbstractions.isDebugDrawEnabled());
                } else if (org.lwjgl.glfw.GLFW.glfwGetKey(win, org.lwjgl.glfw.GLFW.GLFW_KEY_P) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    mc.setScreen(new LanternDebugScreen());
                }
            }
            LanternClientLogic.tickLanternPhysics(mc);
        }
    }
}
