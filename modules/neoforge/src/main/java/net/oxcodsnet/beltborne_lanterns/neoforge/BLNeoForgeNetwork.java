package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.LampConfigSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;


/**
 * Registers server-side network payloads on the MOD bus.
 *
 * <p>Runs on both dedicated and integrated servers so the toggle payload is
 * handled in singleplayer as well.</p>
 */
@EventBusSubscriber(modid = BLMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class BLNeoForgeNetwork {
    private BLNeoForgeNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Register payloads (network version "1")
        var registrar = event.registrar("1");
        registrar.playToClient(BeltSyncPayload.ID, BeltSyncPayload.CODEC, (payload, ctx) -> { /* no-op on server */ });
        registrar.playToClient(LampConfigSyncPayload.ID, LampConfigSyncPayload.CODEC, (payload, ctx) -> { /* no-op */ });
        registrar.playToServer(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC, (payload, ctx) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.player();
            ctx.enqueueWork(() -> {
                ItemStack stack = player.getMainHandStack();
                boolean hasLamp = BeltState.hasLamp(player);
                if (!hasLamp && !LampRegistry.isLamp(stack)) {
                    stack = player.getOffHandStack();
                    if (!LampRegistry.isLamp(stack)) return;
                }
                Item nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
            });
        });
    }
}
