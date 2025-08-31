package net.oxcodsnet.beltborne_lanterns.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;
import net.oxcodsnet.beltborne_lanterns.common.network.ToggleLanternPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.server.BeltLanternServer;


/**
 * Registers network payloads on the MOD bus for the dedicated server side.
 */
@EventBusSubscriber(modid = BLMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public final class BLNeoForgeNetwork {
    private BLNeoForgeNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Register payloads (network version "1")
        var registrar = event.registrar("1");
        registrar.playToClient(BeltSyncPayload.ID, BeltSyncPayload.CODEC, (payload, ctx) -> { /* no-op on server */ });
        registrar.playToServer(ToggleLanternPayload.ID, ToggleLanternPayload.CODEC, (payload, ctx) -> {
            ServerPlayerEntity player = (ServerPlayerEntity) ctx.player();
            ctx.enqueueWork(() -> {
                ItemStack stack = player.getMainHandStack();
                boolean hasLantern = BeltState.hasLantern(player);
                if (!hasLantern && !stack.isOf(Items.LANTERN)) {
                    stack = player.getOffHandStack();
                    if (!stack.isOf(Items.LANTERN)) return;
                }
                boolean nowHas = BeltLanternServer.toggleLantern(player, stack);
                BeltNetworking.broadcastBeltState(player, nowHas);
            });
        });
    }
}
