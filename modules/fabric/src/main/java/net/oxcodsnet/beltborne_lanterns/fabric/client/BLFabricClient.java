package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternBeltFeatureRenderer;
import net.oxcodsnet.beltborne_lanterns.common.client.ClientBeltPlayers;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientLogic;
import net.oxcodsnet.beltborne_lanterns.common.client.LanternClientScreens;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;
import org.lwjgl.glfw.GLFW;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.UUID;

public final class BLFabricClient implements ClientModInitializer {
    // Keybindings
    private static KeyBinding openConfigKey;
    private static KeyBinding toggleDebugKey;
    private static KeyBinding openDebugEditorKey;
    private static KeyBinding toggleLanternKey;

    @Override
    public void onInitializeClient() {
        // Load the lamp registry from the client's config file on startup.
        // This makes the config screen work before joining a world.
        LampRegistry.init();
        BLMod.LOGGER.info("Client initialization started [Fabric]");

        // Rebuild registry after client joins a server (tags/registries are synced at this point)
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(()->{
                ClientBeltPlayers.clear();
                LanternSwingManager.clearAll();
                LampRegistry.init();
            });
        });

        // Clear caches on disconnect as well
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MinecraftClient.getInstance().execute(() -> {
                ClientBeltPlayers.clear();
                LanternSwingManager.clearAll();
            });
        });

        // Register network receiver: updates local client set
        ClientPlayNetworking.registerGlobalReceiver(BeltSyncPayload.ID, (payload, context) -> {
            UUID uuid = payload.playerUuid();
            Item lamp = payload.lampId() != null ? Registries.ITEM.get(payload.lampId()) : null;
            MinecraftClient.getInstance().execute(() -> {
                ClientBeltPlayers.setLamp(uuid, lamp);
            });
        });

        // This receiver handles lamp configs sent from a dedicated server.
        ClientPlayNetworking.registerGlobalReceiver(LampConfigSyncPayload.ID, (payload, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                var cliCfg = BLClientConfigAccess.get();
                cliCfg.extraLampLight.clear();
                payload.lamps().forEach((id, lum) -> cliCfg.extraLampLight.add(new BLClientConfig.ExtraLampEntry(id.toString(), lum)));
                BLClientConfigAccess.save();

                // Re-initialize the lamp registry with the new data from the server.
                LampRegistry.init();
            });
        });

        // Register a feature renderer for players to draw the lantern on the belt
        @SuppressWarnings("unchecked")
        LivingEntityFeatureRendererRegistrationCallback playerFeatureRendererCallback = (entityType, renderer, helper, context) -> {
            if (entityType == EntityType.PLAYER) {
                helper.register(new LanternBeltFeatureRenderer(renderer));
            }
        };
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(playerFeatureRendererCallback);

        // Clean up swing manager state when a player entity is unloaded
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof PlayerEntity) {
                LanternSwingManager.removePlayer(entity.getUuid());
            }
        });

        // Optionally register dynamic lights for the belt lantern when LambDynamicLights is present
        boolean hasLamb = FabricLoader.getInstance().isModLoaded("lambdynlights");

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

        // Keybind to toggle belt lantern (default: B)
        toggleLanternKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.beltborne_lanterns.toggle_lantern",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B,
                "category.beltborne_lanterns"
        ));

        // Wire platform abstractions so common renderer can query state/debug
        BLClientAbstractions.init(ClientBeltPlayers::getLamp);

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

            if (toggleLanternKey.wasPressed()) {
                ClientPlayNetworking.send(new ToggleLanternPayload());
            }

            // Update lantern physics states for players who have a belt lantern
            LanternClientLogic.tickLanternPhysics(client);
        });

        // Final client-ready log (concise, useful to players)
        String dyn = hasLamb ? "enabled" : "disabled";
        BLMod.LOGGER.info("Client ready [Fabric]. Dynamic lights: {}.", dyn);
    }

    // Debug flag is maintained via BLClientAbstractions
}
