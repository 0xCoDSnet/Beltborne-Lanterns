package net.oxcodsnet.beltborne_lanterns.fabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

import net.minecraft.client.render.model.json.ModelTransformationMode;

public class LanternBeltFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private static final ItemStack LANTERN_STACK = new ItemStack(Items.LANTERN);
    private final ItemRenderer itemRenderer;

    public LanternBeltFeatureRenderer(FeatureRendererContext<T, M> context, ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!(entity instanceof PlayerEntity player)) return;
        if (!ExampleModFabricClient.clientHasLantern(player)) return;

        matrices.push();
        // Rotate with the body so it sticks to the torso
        this.getContextModel().body.rotate(matrices);

        // Position on the right hip area (tweak as needed)
        matrices.translate(0.25F, 0.9F, -0.15F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
        matrices.scale(0.8F, 0.8F, 0.8F);

        // Render as fixed/ground-like item
        this.itemRenderer.renderItem(LANTERN_STACK, ModelTransformationMode.FIXED, light, 0, matrices, vertexConsumers, entity.getWorld(), entity.getId());

        matrices.pop();
    }
}

