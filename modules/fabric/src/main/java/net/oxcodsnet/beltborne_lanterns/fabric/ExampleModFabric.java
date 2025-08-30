package net.oxcodsnet.beltborne_lanterns.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

import net.oxcodsnet.beltborne_lanterns.ExampleMod;
import net.oxcodsnet.beltborne_lanterns.common.BeltState;
import net.oxcodsnet.beltborne_lanterns.common.network.BeltSyncPayload;

public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        ExampleMod.init();

        // Register payload type (S2C) for belt sync on server and integrated client
        PayloadTypeRegistry.playS2C().register(BeltSyncPayload.ID, BeltSyncPayload.CODEC);

        // Register shift+right-click with lantern toggle logic (server authoritative)
        UseItemCallback.EVENT.register((player, world, hand) -> toggleLanternOnUse(player, world, hand));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> toggleLanternOnBlock(player, world, hand));

        // When a player joins, sync known belt states of all players to them
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity joining = handler.getPlayer();
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                boolean has = BeltState.hasLantern(other);
                BeltNetworking.sendTo(joining, other.getUuid(), has);
            }
        });
    }

    private static TypedActionResult<ItemStack> toggleLanternOnUse(net.minecraft.entity.player.PlayerEntity player, net.minecraft.world.World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return TypedActionResult.pass(stack);
        if (!player.isSneaking()) return TypedActionResult.pass(stack);
        if (!stack.isOf(Items.LANTERN)) return TypedActionResult.pass(stack);
        doToggle((ServerPlayerEntity) player, stack);
        return TypedActionResult.success(stack, world.isClient());
    }

    private static net.minecraft.util.ActionResult toggleLanternOnBlock(net.minecraft.entity.player.PlayerEntity player, net.minecraft.world.World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient()) return net.minecraft.util.ActionResult.PASS;
        if (!player.isSneaking()) return net.minecraft.util.ActionResult.PASS;
        if (!stack.isOf(Items.LANTERN)) return net.minecraft.util.ActionResult.PASS;
        doToggle((ServerPlayerEntity) player, stack);
        return net.minecraft.util.ActionResult.SUCCESS;
    }

    private static void doToggle(ServerPlayerEntity player, ItemStack stackInHand) {
        boolean has = BeltState.hasLantern(player);
        if (!has) {
            if (!player.isCreative()) {
                stackInHand.decrement(1);
            }
            BeltState.setHasLantern(player, true);
            BeltNetworking.broadcastBeltState(player, true);
        } else {
            if (!player.isCreative()) {
                player.giveItemStack(new ItemStack(Items.LANTERN));
            }
            BeltState.setHasLantern(player, false);
            BeltNetworking.broadcastBeltState(player, false);
        }
    }
}
