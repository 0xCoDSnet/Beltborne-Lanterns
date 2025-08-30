package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.oxcodsnet.beltborne_lanterns.common.LambDynLightsCompat;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternBeltFeatureRenderer;
import net.oxcodsnet.beltborne_lanterns.common.client.ClientBeltPlayers;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientLogic;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientScreens;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class BLFabricClient implements ClientModInitializer {
    // Keybindings
    private static KeyBinding openConfigKey;
    private static KeyBinding toggleDebugKey;
    private static KeyBinding openDebugEditorKey;

    @Override
    public void onInitializeClient() {
        // Register network receiver: updates local client set
        ClientPlayNetworking.registerGlobalReceiver(BeltSyncPayload.ID, (payload, context) -> {
            UUID uuid = payload.playerUuid();
            boolean has = payload.hasLantern();
            MinecraftClient.getInstance().execute(() -> {
                ClientBeltPlayers.setHas(uuid, has);
            });
        });

        // Register a feature renderer for players to draw the lantern on the belt
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (entityType == EntityType.PLAYER) {
                helper.register(new LanternBeltFeatureRenderer(renderer));
            }
        });

        // Optionally register dynamic lights for the belt lantern when LambDynamicLights is present
        if (FabricLoader.getInstance().isModLoaded("lambdynlights")) {
            LambDynLightsCompat.init();
        }

        // Keybind to open config (default: L)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.beltborne_lanterns.open_config",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L,
                "category.beltborne_lanterns"
        ));

        // Keybind to toggle debug gizmos (default: K)
        toggleDebugKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.beltborne_lanterns.toggle_debug",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                "category.beltborne_lanterns"
        ));

        // Keybind to open lantern debug editor (default: P)
        openDebugEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.beltborne_lanterns.open_debug",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P,
                "category.beltborne_lanterns"
        ));

        // Wire platform abstractions so common renderer can query state/debug
        BLClientAbstractions.init(ClientBeltPlayers::hasLantern, BLClientAbstractions::isDebugDrawEnabled);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(LanternClientScreens.openConfig(client.currentScreen));
                }
            }

            if (toggleDebugKey.wasPressed()) {
                if (net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess.get().debug) {
                    BLClientAbstractions.setDebugDrawEnabled(!BLClientAbstractions.isDebugDrawEnabled());
                }
            }

            if (openDebugEditorKey.wasPressed()) {
                if (net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess.get().debug) {
                    if (client.currentScreen == null) {
                        client.setScreen(new net.oxcodsnet.beltborne_lanterns.common.client.ui.LanternDebugScreen());
                    }
                }
            }

            // Update lantern physics states for players who have a belt lantern
            LanternClientLogic.tickLanternPhysics(client);
        });
    }

    // Debug flag is maintained via BLClientAbstractions
}
