package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;

/**
 * Common per-tick logic for lantern client-side updates.
 */
public final class LanternClientLogic {
    private LanternClientLogic() {}

    public static void tickLanternPhysics(MinecraftClient mc) {
        if (mc.world == null) return;
        final float dt = 1.0f / 20.0f;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (BLClientAbstractions.clientHasLantern(p)) {
                LanternSwingManager.tickPlayer(p, dt, BLConfigs.get().rotXDeg);
            }
        }
    }
}

