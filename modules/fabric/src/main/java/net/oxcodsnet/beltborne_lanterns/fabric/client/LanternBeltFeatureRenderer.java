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
import net.oxcodsnet.beltborne_lanterns.fabric.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.fabric.config.BLConfigHolder;

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
        // Рендерим только для игрока, у которого есть фонарь
        if (!(entity instanceof PlayerEntity player)) return;
        if (!ExampleModFabricClient.clientHasLantern(player)) return;

        matrices.push();

        // 1) Приклеиваемся к торсу
        this.getContextModel().body.rotate(matrices);

        // 2) Читаем конфиг
        BLClientConfig c = BLConfigHolder.get();
        final float offX = c.fOffsetX();
        final float offY = c.fOffsetY();
        final float offZ = c.fOffsetZ();
        final float pivX = c.fPivotX();
        final float pivY = c.fPivotY();
        final float pivZ = c.fPivotZ();
        final float s    = c.fScale();

        // 3) Больше никаких автосмещений/покачиваний — чистая математика

        // 4) Переводим локальную сцену в offset (смещение на поясе)
        matrices.translate(offX, offY, offZ);

        // 5) Рисуем гизмо ТОЛЬКО в pivot (если включён дебаг)
        if (ExampleModFabricClient.isDebugDrawEnabled()) {
            matrices.push();
            matrices.translate(pivX, pivY, pivZ);
            drawAxesAndAnchor(matrices, vertexConsumers, 0.25f);
            matrices.pop();
        }

        // 6) «бутерброд» вокруг pivot: туда → scale/rotate → обратно
        matrices.translate(pivX, pivY, pivZ);

        // базовый поворот: блок «смотрит» корректно относительно торса
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));

        // масштаб вокруг якоря
        matrices.scale(s, s, s);

        // пользовательские углы
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(c.rotXDeg));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(c.rotYDeg));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(c.rotZDeg));

        matrices.translate(-pivX, -pivY, -pivZ);

        // 7) Рендер фонаря как сущности блока
        BlockRenderManager brm = MinecraftClient.getInstance().getBlockRenderManager();
        BlockState state = Blocks.LANTERN.getDefaultState(); // или свой BlockState при необходимости
        brm.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

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
