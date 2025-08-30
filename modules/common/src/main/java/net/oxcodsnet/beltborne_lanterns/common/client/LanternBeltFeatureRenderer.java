package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.RotationAxis;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs;
import net.oxcodsnet.beltborne_lanterns.common.physics.LanternSwingManager;

public class LanternBeltFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private static final BlockState LANTERN_STATE = Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false);

    public LanternBeltFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            T entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!BLClientAbstractions.clientHasLantern(player)) return;

        BLConfig c = BLConfigs.get();

        matrices.push();

        // Attach to torso
        this.getContextModel().body.rotate(matrices);

        final float offX = c.fOffsetX();
        final float offY = c.fOffsetY();
        final float offZ = c.fOffsetZ();
        final float pivX = c.fPivotX();
        final float pivY = c.fPivotY();
        final float pivZ = c.fPivotZ();
        final float s    = c.fScale();

        matrices.translate(offX, offY, offZ);

        if (BLClientAbstractions.isDebugDrawEnabled()) {
            matrices.push();
            matrices.translate(pivX, pivY, pivZ);
            BLDebugRender.drawAxesAndAnchor(matrices, vertexConsumers, 0.25f);
            matrices.pop();
        }

        matrices.translate(pivX, pivY, pivZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
        matrices.scale(s, s, s);

        float dynX = LanternSwingManager.getXDeg(player.getUuid());
        float dynZ = LanternSwingManager.getZDeg(player.getUuid());
        float baseX = LanternSwingManager.getBaseXDeg(player.getUuid());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(baseX + dynX));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotYDeg));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.rotZDeg + dynZ));

        matrices.translate(-pivX, -pivY, -pivZ);

        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        BlockState state = LANTERN_STATE; // render vanilla lantern, or customize as needed
        brm.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}

