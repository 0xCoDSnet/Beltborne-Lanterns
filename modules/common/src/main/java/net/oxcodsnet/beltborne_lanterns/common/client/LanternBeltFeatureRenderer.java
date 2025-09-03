package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;

import java.util.UUID;

public class LanternBeltFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    // The lantern model might need a rotation adjustment to face forward.
    private static final float MODEL_Y_ROTATION_DEGREES = 180f;
    private static boolean UUID_LOOKUP_WARNED = false;

    @SuppressWarnings("unchecked")
    public LanternBeltFeatureRenderer(FeatureRendererContext<?, ?> context) {
        // Cast to the exact generic pair expected by the superclass.
        super((FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) context);
    }

    @Override
    public void render(MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       PlayerEntityRenderState state,
                       float limbAngle,
                       float limbDistance) {
        // MC 1.21+ feature renderers receive a render-state, not the entity.
        // Determine the rendered player's UUID from the state when possible.
        UUID subject = null;
        // First, try accessor injected into render state
        if (state instanceof RenderStatePlayerUuidAccess acc) {
            subject = acc.bl$getPlayerUuid();
        }
        // Next, try the mixin-provided map that binds states to entities at updateRenderState time
        if (subject == null) {
            subject = RenderStateUUIDMap.get(state);
        }
        // If unavailable, try to obtain UUID from the render state via reflection to be resilient to mapping changes
        try {
            Class<?> cls = state.getClass();
            // Candidate fields: prefer direct UUID; otherwise GameProfile
            String[] uuidFieldCandidates = {"uuid", "profileId", "gameProfileId", "playerUuid"};
            for (String name : uuidFieldCandidates) {
                try {
                    var f = ReflectUtilBL.findFieldRecursive(cls, name);
                    f.setAccessible(true);
                    Object v = f.get(state);
                    if (v instanceof UUID u) {
                        subject = u;
                        break;
                    }
                } catch (NoSuchFieldException ignored2) { /* try next */ }
            }
            if (subject == null) {
                String[] profileFieldCandidates = {"gameProfile", "profile"};
                for (String name : profileFieldCandidates) {
                    try {
                        var f = ReflectUtilBL.findFieldRecursive(cls, name);
                        f.setAccessible(true);
                        Object v = f.get(state);
                        // Avoid compile-time dep: use reflection to call getId
                        if (v != null) {
                            try {
                                var m = v.getClass().getMethod("getId");
                                Object id = m.invoke(v);
                                if (id instanceof UUID u) {
                                    subject = u;
                                    break;
                                }
                            } catch (ReflectiveOperationException ignored3) {
                            }
                        }
                    } catch (NoSuchFieldException ignored4) { /* try next */ }
                }
            }
        } catch (Throwable ignored) {
        }

        if (subject == null && MinecraftClient.getInstance().player != null) {
            // Fallback: use local player if state field mapping changes
            subject = MinecraftClient.getInstance().player.getUuid();
            if (!UUID_LOOKUP_WARNED) {
                net.oxcodsnet.beltborne_lanterns.BLMod.LOGGER.warn("Beltborne Lanterns: falling back to local player UUID in feature renderer; mixin may be needed for this mapping.");
                UUID_LOOKUP_WARNED = true;
            }
        }

        if (subject == null) return;

        // Only render if that specific player actually has a belt lamp (client cache)
        var lampItem = ClientBeltPlayers.getLamp(subject);
        if (lampItem == null) return;

        BLConfig c = BLConfigs.get();

        matrices.push();

        // Attach to torso (ModelPart#rotate(MatrixStack) was removed in 1.21.5)
        applyModelPart(matrices, this.getContextModel().body);

        final float offX = c.fOffsetX();
        final float offY = c.fOffsetY();
        final float offZ = c.fOffsetZ();
        final float pivX = c.fPivotX();
        final float pivY = c.fPivotY();
        final float pivZ = c.fPivotZ();
        final float s = c.fScale();

        matrices.translate(offX, offY, offZ);

        if (BLClientAbstractions.isDebugDrawEnabled()) {
            matrices.push();
            matrices.translate(pivX, pivY, pivZ);
            BLDebugRender.drawAxesAndAnchor(matrices, vertexConsumers, 0.25f);
            matrices.pop();
        }

        matrices.translate(pivX, pivY, pivZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MODEL_Y_ROTATION_DEGREES));
        matrices.scale(s, s, s);

        float dynX = LanternSwingManager.getXDeg(subject);
        float dynZ = LanternSwingManager.getZDeg(subject);
        float baseX = LanternSwingManager.getBaseXDeg(subject);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(baseX + dynX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotYDeg));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.rotZDeg + dynZ));

        matrices.translate(-pivX, -pivY, -pivZ);

        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        BlockState blockState = LampRegistry.getState(lampItem);
        brm.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
    private static void applyModelPart(MatrixStack matrices, ModelPart part) {
        // Apply the part's origin and rotation to the matrix stack
        part.applyTransform(matrices);
    }
}
// Reflection helpers (package-private)
final class ReflectUtilBL {
    private ReflectUtilBL() {
    }

    static java.lang.reflect.Field findFieldRecursive(Class<?> cls, String name) throws NoSuchFieldException {
        Class<?> c = cls;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
