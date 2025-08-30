package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.oxcodsnet.beltborne_lanterns.fabric.config.BLConfigHolder;

public class LanternBeltFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private static final BlockState LANTERN_STATE = Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false);

    public LanternBeltFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!ExampleModFabricClient.clientHasLantern(player)) return;

        matrices.push();
        // Stick to torso rotation
        this.getContextModel().body.rotate(matrices);

        // Apply configurable transforms (AutoConfig-backed)
        var cfg = BLConfigHolder.get();

        // Base offset from config
        Vec3d offset = new Vec3d(cfg.fOffsetX(), cfg.fOffsetY(), cfg.fOffsetZ());
        // Local pivot (for rotation center)
        Vec3d pivot = new Vec3d(cfg.fPivotX(), cfg.fPivotY(), cfg.fPivotZ());

        // Adjust for player hitbox changes (e.g., crouching)
        double heightDiff = player.getHeight() - 1.8D;
        if (heightDiff != 0.0D) {
            offset = offset.add(0.0D, heightDiff * 0.5D, 0.0D);
        }

        // Simple walk swing using limb animation
        float walkSwing = MathHelper.sin(limbAngle * 0.6662F) * 0.15F * limbDistance;
        offset = offset.add(0.0D, 0.0D, walkSwing);

        // Vertical bobbing from jumping/falling
        double vy = player.getVelocity().y;
        offset = offset.add(0.0D, MathHelper.clamp(vy * 0.1D, -0.15D, 0.15D), 0.0D);

        // Sneaking lowers the lantern slightly
        if (player.isInSneakingPose()) {
            offset = offset.add(0.0D, -0.1D, 0.0D);
        }

        // Prevent clipping through blocks by checking a small AABB at the lantern position
        Vec3d worldPos = player.getLerpedPos(tickDelta).add(offset);
        Box lanternBox = new Box(worldPos.x - 0.15D, worldPos.y - 0.2D, worldPos.z - 0.15D,
                worldPos.x + 0.15D, worldPos.y + 0.3D, worldPos.z + 0.15D);
        if (!player.getWorld().isSpaceEmpty(player, lanternBox)) {
            // Push the lantern inward to avoid clipping
            offset = offset.multiply(0.5D, 1.0D, 0.5D);
        }

        // Apply final transforms: translate to body offset, then rotate around the chosen local pivot
        matrices.translate(offset.x, offset.y, offset.z);
        // move to pivot, rotate, move back
        matrices.translate(pivot.x, pivot.y, pivot.z);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cfg.rotXDeg));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(cfg.rotYDeg));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(cfg.rotZDeg));
        matrices.translate(-pivot.x, -pivot.y, -pivot.z);

        // Debug gizmos: anchor point + axes at current pivot (local origin)
        if (ExampleModFabricClient.isDebugDrawEnabled()) {
            // Draw axes at origin (post-offset, pre-rotation) and at pivot for clarity
            //drawAxesAndAnchor(matrices, vertexConsumers, 0.25f);
            matrices.push();
            matrices.translate(pivot.x, pivot.y, pivot.z);
            drawAxesAndAnchor(matrices, vertexConsumers, 0.25f);
            matrices.pop();
        }

        // для перемещения "рендера" ламбы
        matrices.translate(0,0,0);

        float s = cfg.fScale();
        matrices.scale(s, s, s);

        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        brm.renderBlockAsEntity(LANTERN_STATE, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }

    private static void drawAxesAndAnchor(MatrixStack matrices, VertexConsumerProvider vertices, float axisLength) {
        VertexConsumer vc = vertices.getBuffer(RenderLayer.getLines());

        // Anchor: small wireframe cube at origin
        float c = axisLength * 0.08f;
        WorldRenderer.drawBox(matrices, vc, -c, -c, -c, c, c, c, 1.0f, 1.0f, 1.0f, 1.0f);

        // Axes as thin wireframe boxes from origin
        float t = c * 0.4f; // half-thickness
        // +X axis (red)
        WorldRenderer.drawBox(matrices, vc, 0.0, -t, -t, axisLength, t, t, 1.0f, 0.25f, 0.25f, 1.0f);
        // +Y axis (green)
        WorldRenderer.drawBox(matrices, vc, -t, 0.0, -t, t, axisLength, t, 0.25f, 1.0f, 0.25f, 1.0f);
        // +Z axis (blue)
        WorldRenderer.drawBox(matrices, vc, -t, -t, 0.0, t, t, axisLength, 0.25f, 0.5f, 1.0f, 1.0f);
    }
}
