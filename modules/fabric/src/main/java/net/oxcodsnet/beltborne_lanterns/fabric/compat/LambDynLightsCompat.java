package net.oxcodsnet.beltborne_lanterns.fabric.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.oxcodsnet.beltborne_lanterns.BLMod;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Direct integration with the LambDynamicLights v4 API.
 * The handler is registered lazily so the dependency remains optional at runtime.
 */
public final class LambDynLightsCompat {
    private static final Identifier HANDLER_ID = new Identifier(BLMod.MOD_ID, "player_lantern");
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean LEGACY_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean REGISTER_EVENT_INVOKED = new AtomicBoolean(false);
    private static final AtomicBoolean POLL_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean POLL_WITH_LAMP_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean POLL_WITHOUT_LAMP_LOGGED = new AtomicBoolean(false);
    private static final AtomicReference<Item> LAST_POLLED_LAMP = new AtomicReference<>();

    private static final EntityLuminance PLAYER_LUMINANCE = new EntityLuminance() {
        @Override
        public EntityLuminance.Type type() {
            return EntityLuminance.Type.VALUE;
        }

        @Override
        public int getLuminance(ItemLightSourceManager items, Entity entity) {
            if (entity instanceof PlayerEntity player) {
                var lamp = BLClientAbstractions.clientLamp(player);
                int luminance = lamp != null ? LampRegistry.getLuminance(lamp) : 0;

                if (BLMod.LOGGER.isDebugEnabled()) {
                    if (POLL_LOGGED.compareAndSet(false, true)) {
                        BLMod.LOGGER.debug("Dynamic lights: LambDynamicLights polled {} (lamp={}, luminance={}).",
                                player.getGameProfile().getName(),
                                lamp != null ? LampRegistry.getId(lamp) : "<none>",
                                luminance);
                    }

                    var lastLamp = LAST_POLLED_LAMP.getAndSet(lamp);
                    if (lamp != null && luminance == 0 && POLL_WITH_LAMP_LOGGED.compareAndSet(false, true)) {
                        BLMod.LOGGER.debug("Dynamic lights: belt lamp {} returned zero luminance (isLamp={}, registry size={}).",
                                LampRegistry.getId(lamp),
                                LampRegistry.isLamp(lamp),
                                LampRegistry.items().size());
                    } else if (lamp == null && POLL_WITHOUT_LAMP_LOGGED.compareAndSet(false, true)) {
                        BLMod.LOGGER.debug("Dynamic lights: LambDynamicLights queried {} but client reports no lamp equipped.",
                                player.getGameProfile().getName());
                    } else if (lamp != null && lamp != lastLamp) {
                        BLMod.LOGGER.debug("Dynamic lights: detected lamp switch for {} -> {} (luminance={}).",
                                player.getGameProfile().getName(),
                                LampRegistry.getId(lamp),
                                luminance);
                    }
                }

                return luminance;
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
        BLMod.LOGGER.info("Dynamic lights: initializing LambDynamicLights v4 integration (context={}, manager={}).",
                context.getClass().getName(),
                manager.getClass().getName());

        manager.onRegisterEvent().register(HANDLER_ID, registerContext -> {
            REGISTER_EVENT_INVOKED.set(true);
            BLMod.LOGGER.info("Dynamic lights: register event invoked (lookup={}).",
                    describeRegistryLookup(registerContext));
            registerContext.register(EntityType.PLAYER, PLAYER_LUMINANCE);
            BLMod.LOGGER.info("Dynamic lights: player luminance handler registered with LambDynamicLights.");
        });
        BLMod.LOGGER.info("Dynamic lights: awaiting LambDynamicLights registry reload to finalize player handler.");

        registerLegacyHandler();
    }

    private static String describeRegistryLookup(EntityLightSourceManager.RegisterContext registerContext) {
        try {
            var method = registerContext.getClass().getMethod("registryLookup");
            var lookup = method.invoke(registerContext);
            return lookup != null ? lookup.getClass().getName() : "<null>";
        } catch (ReflectiveOperationException ignored) {
            try {
                @SuppressWarnings("deprecation")
                var access = registerContext.registryAccess();
                return access != null ? access.getClass().getName() : "<null>";
            } catch (Throwable t) {
                return "<unavailable>";
            }
        }
    }

    public static void registerLegacyHandler() {
        if (!LEGACY_REGISTERED.compareAndSet(false, true)) {
            return;
        }

        try {
            DynamicLightHandlers.registerDynamicLightHandler(
                    EntityType.PLAYER,
                    DynamicLightHandler.makeHandler(
                            player -> {
                                var lamp = BLClientAbstractions.clientLamp(player);
                                return lamp != null ? LampRegistry.getLuminance(lamp) : 0;
                            },
                            player -> false
                    )
            );
            BLMod.LOGGER.info("Dynamic lights: registered legacy LambDynamicLights handler");
        } catch (Throwable t) {
            BLMod.LOGGER.debug("Dynamic lights: legacy handler unavailable.", t);
        }
    }
}