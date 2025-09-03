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

    /**
     * Draws axes gizmo and a small cube at origin.
     * In MC 1.21, WorldRenderer.drawBox was removed from the public API.
     * To keep builds green without pulling in custom line-rendering code,
     * this method becomes a no-op debug stub.
     */
    public static void drawAxesAndAnchor(MatrixStack matrices, VertexConsumerProvider vertices, float axisLength) {
        // Intentionally left blank on 1.21+ to avoid API churn.
    }
}
