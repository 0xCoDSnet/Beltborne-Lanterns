package net.oxcodsnet.beltborne_lanterns.common;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;

/**
 * Entrypoint for LambDynamicLights 4.x API.
 *
 * @see <a href="https://lambdaurora.dev/projects/lambdynamiclights/docs/v4/java.html#register-an-entity-luminance-provider">
 *     LambDynamicLights 4.x documentation on entity luminance
 * </a>
 */
public final class LambDynLightsInitializer implements DynamicLightsInitializer {
    static final EntityLuminance.Type PLAYER_LANTERN = EntityLuminance.Type.registerSimple(
            Identifier.of(BLMod.MOD_ID, "player_lantern"),
            PlayerLanternLuminance.INSTANCE
    );

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        EntityLightSourceManager mgr = context.entityLightSourceManager();
        mgr.onRegisterEvent().register(ctx -> ctx.register(EntityType.PLAYER, PlayerLanternLuminance.INSTANCE));
        BLMod.LOGGER.info("Dynamic lights: integrated via LambDynamicLights 4.x (API entrypoint)");
        LambDynLightsCompat.markInitialized();
    }

    private final static class PlayerLanternLuminance implements EntityLuminance {
        static final PlayerLanternLuminance INSTANCE = new PlayerLanternLuminance();

        @Override
        public Type type() {
            return PLAYER_LANTERN;
        }

        @Override
        public int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
            if (entity instanceof PlayerEntity player) {
                Item lamp = BLClientAbstractions.clientLamp(player);
                return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
            }
            return 0;
        }
    }
}
