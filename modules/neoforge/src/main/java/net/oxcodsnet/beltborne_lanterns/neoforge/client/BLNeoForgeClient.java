package net.oxcodsnet.beltborne_lanterns.neoforge.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.client.ClientBeltPlayers;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternBeltFeatureRenderer;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientLogic;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientScreens;
import net.oxcodsnet.beltborne_lanterns.common.client.ui.LanternDebugScreen;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.neoforged.neoforge.network.PacketDistributor;

import net.oxcodsnet.beltborne_lanterns.common.config.BLLampConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;

import java.util.UUID;

@EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BLNeoForgeClient {
    // no per-loader state; use common ClientBeltPlayers

    private BLNeoForgeClient() {}

    // Keybindings
    private static KeyBinding openConfigKey;
    private static KeyBinding toggleDebugKey;
    private static KeyBinding openDebugEditorKey;
    private static KeyBinding toggleLanternKey;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Provide platform bridges for common renderer
        BLClientAbstractions.init(ClientBeltPlayers::getLamp, BLClientAbstractions::isDebugDrawEnabled);


        // Register the config screen with NeoForge's extension point
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (mc, parent) -> LanternClientScreens.openConfig(parent)
        );
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openConfigKey = new KeyBinding(
                "key.beltborne_lanterns.open_config",
                InputUtil.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_L,
                "category.beltborne_lanterns"
        );
        toggleDebugKey = new KeyBinding(
                "key.beltborne_lanterns.toggle_debug",
                InputUtil.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_K,
                "category.beltborne_lanterns"
        );
        openDebugEditorKey = new KeyBinding(
                "key.beltborne_lanterns.open_debug",
                InputUtil.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_P,
                "category.beltborne_lanterns"
        );
        toggleLanternKey = new KeyBinding(
                "key.beltborne_lanterns.toggle_lantern",
                InputUtil.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_B,
                "category.beltborne_lanterns"
        );
        event.register(openConfigKey);
        event.register(toggleDebugKey);
        event.register(openDebugEditorKey);
        event.register(toggleLanternKey);
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
                    Item lamp = payload.lampId() != null ? Registries.ITEM.get(payload.lampId()) : null;
                    ClientBeltPlayers.setLamp(uuid, lamp);
                }
        );
        registrar.playToClient(
                LampConfigSyncPayload.ID,
                LampConfigSyncPayload.CODEC,
                (payload, ctx) -> {
                    var lampCfg = BLLampConfigAccess.get();
                    lampCfg.extraLampLight.clear();
                    payload.lamps().forEach((id, lum) -> lampCfg.extraLampLight.put(id.toString(), lum));
                    var cliCfg = BLClientConfigAccess.get();
                    cliCfg.extraLampLight.clear();
                    payload.lamps().forEach((id, lum) -> cliCfg.extraLampLight.put(id.toString(), lum));
                    LampRegistry.init();
                }
        );
        registrar.playToServer(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC, (payload, ctx) -> { /* no-op on client */ });
    }

    @EventBusSubscriber(modid = BLMod.MOD_ID, value = Dist.CLIENT)
    public static final class ClientBus {
        private ClientBus() {}

        @SubscribeEvent
        public static void onEntityLeave(EntityLeaveLevelEvent event) {
            if (event.getEntity() instanceof PlayerEntity) {
                LanternSwingManager.removePlayer(event.getEntity().getUuid());
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post e) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (openConfigKey != null && openConfigKey.wasPressed()) {
                if (mc.currentScreen == null) {
                    mc.setScreen(LanternClientScreens.openConfig(mc.currentScreen));
                }
            }

            if (toggleDebugKey != null && toggleDebugKey.wasPressed()) {
                if (BLClientConfigAccess.get().debug) {
                    BLClientAbstractions.setDebugDrawEnabled(!BLClientAbstractions.isDebugDrawEnabled());
                }
            }

            if (openDebugEditorKey != null && openDebugEditorKey.wasPressed()) {
                if (BLClientConfigAccess.get().debug) {
                    if (mc.currentScreen == null) {
                        mc.setScreen(new LanternDebugScreen());
                    }
                }
            }

            if (toggleLanternKey != null && toggleLanternKey.wasPressed()) {
                PacketDistributor.sendToServer(new ToggleLanternPayload());
            }

            LanternClientLogic.tickLanternPhysics(mc);
        }
    }
}
