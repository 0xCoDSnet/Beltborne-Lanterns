package net.oxcodsnet.beltborne_lanterns.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.oxcodsnet.beltborne_lanterns.common.client.RenderStateUUIDMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void bl$captureUUID(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && state instanceof PlayerEntityRenderState) {
            // Store via accessor on the state itself
            if (state instanceof net.oxcodsnet.beltborne_lanterns.common.client.RenderStatePlayerUuidAccess acc) {
                acc.bl$setPlayerUuid(entity.getUuid());
            }
            // Keep weak map as a secondary fallback path
            RenderStateUUIDMap.put(state, entity.getUuid());
        }
    }
}
