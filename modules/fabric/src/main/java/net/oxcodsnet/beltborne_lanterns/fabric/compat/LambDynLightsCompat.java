package net.oxcodsnet.beltborne_lanterns.fabric.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Direct integration with the LambDynamicLights v4 API.
 * The handler is registered lazily so the dependency remains optional at runtime.
 */
public final class LambDynLightsCompat {
    private static final Identifier HANDLER_ID = new Identifier(BLMod.MOD_ID, "player_lantern");
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private static final EntityLuminance PLAYER_LUMINANCE = new EntityLuminance() {
        @Override
        public EntityLuminance.Type type() {
            return EntityLuminance.Type.VALUE;
        }

        @Override
        public int getLuminance(ItemLightSourceManager items, Entity entity) {
            if (entity instanceof PlayerEntity player) {
                var lamp = BLClientAbstractions.clientLamp(player);
                return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
            }
            return 0;
        }
    };

    private LambDynLightsCompat() {
    }

    public static void register(DynamicLightsContext context) {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        EntityLightSourceManager manager = context.entityLightSourceManager();
        manager.onRegisterEvent().register(HANDLER_ID, registerContext -> registerContext.register(EntityType.PLAYER, PLAYER_LUMINANCE));
        BLMod.LOGGER.info("Dynamic lights: integrated via LambDynamicLights API");
    }
}
