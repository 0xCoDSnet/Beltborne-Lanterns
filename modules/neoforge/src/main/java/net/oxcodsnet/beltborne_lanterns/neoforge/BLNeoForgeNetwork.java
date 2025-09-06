package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.client.ClientBeltPlayers;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;

import java.util.UUID;


/**
 * Registers all network payloads on the MOD bus.
 *
 * <p>Runs on both client and server sides.</p>
 */
@EventBusSubscriber(modid = BLMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class BLNeoForgeNetwork {
    private BLNeoForgeNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Register payloads (network version "1")
        var registrar = event.registrar("1");

        // C2S Payloads (Client to Server)
        registrar.playToServer(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC, (payload, ctx) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.player();
            ctx.enqueueWork(() -> {
                // TODO: Accessories compat moved to a separate module
                // boolean handledByAccessories = false;
                // try {
                //     handledByAccessories = net.oxcodsnet.beltborne_lanterns.neoforge.compat.AccessoriesCompatNeoForge.tryToggleViaAccessories(player);
                // } catch (Throwable ignored) {
                //     // Accessories not installed or API unavailable; fall through to default logic
                // }

                // if (handledByAccessories) return;

                ItemStack stack = player.getMainHandStack();
                boolean hasLamp = BeltState.hasLamp(player);
                if (!hasLamp && !LampRegistry.isLamp(stack)) {
                    stack = player.getOffHandStack();
                    if (!LampRegistry.isLamp(stack)) return;
                }
                Item nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
                if (nowHas != null) {
                    // TODO: Accessories compat moved to a separate module
                    // try {
                    //     net.oxcodsnet.beltborne_lanterns.neoforge.compat.AccessoriesCompatNeoForge.syncToggleOnToAccessories(player);
                    // } catch (Throwable ignored) {
                    //     // Accessories not installed — ignore
                    // }
                }
            });
        });

        // S2C Payloads (Server to Client)
        // The handlers for these are guaranteed to run on the client side only.
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
                    // This receiver handles lamp configs sent from a dedicated server.
                    ctx.enqueueWork(() -> {
                        var cliCfg = BLClientConfigAccess.get();
                        cliCfg.extraLampLight.clear();
                        payload.lamps().forEach((id, lum) -> cliCfg.extraLampLight.add(new BLClientConfig.ExtraLampEntry(id.toString(), lum)));
                        BLClientConfigAccess.save();

                        // Re-initialize the lamp registry with the new data from the server.
                        LampRegistry.init();
                    });
                }
        );
    }
}
