package net.oxcodsnet.beltborne_lanterns.common.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Client-side common debug helpers (platform-agnostic).
 */
public final class BLDebugRender {
    private BLDebugRender() {}

    /** Draws axes gizmo and a small cube at origin. */
    public static void drawAxesAndAnchor(MatrixStack matrices, VertexConsumerProvider vertices, float axisLength) {
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

