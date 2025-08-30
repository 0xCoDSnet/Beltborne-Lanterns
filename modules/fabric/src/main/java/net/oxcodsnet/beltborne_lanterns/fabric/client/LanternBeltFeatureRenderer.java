package net.oxcodsnet.beltborne_lanterns.fabric.client;

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

public class LanternBeltFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private static final BlockState LANTERN_STATE = Blocks.LANTERN.getDefaultState().with(Properties.HANGING, false);

    public LanternBeltFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!ExampleModFabricClient.clientHasLantern(player)) return;

        matrices.push();
        // Stick to torso rotation
        this.getContextModel().body.rotate(matrices);

        // Right hip position; tweak as needed
        matrices.translate(0.26F, 0.92F, -0.14F);

        // Orient upright and slightly facing outward
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
        matrices.scale(0.75F, 0.75F, 0.75F);

        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        brm.renderBlockAsEntity(LANTERN_STATE, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}
