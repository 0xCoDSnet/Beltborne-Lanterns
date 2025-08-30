package net.oxcodsnet.beltborne_lanterns.fabric.client.ui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.oxcodsnet.beltborne_lanterns.fabric.client.ExampleModFabricClient;
import net.oxcodsnet.beltborne_lanterns.fabric.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.fabric.config.BLConfigHolder;

/**
 * Simple in-game debug UI to tweak lantern FeatureRenderer transforms.
 * Lets you adjust: offset (world), pivot (local), rotation, scale.
 *
 * Controls:
 * - Buttons +/- per axis for offset, pivot, rotation, scale
 * - Keyboard: Arrow keys (X/Z), PageUp/PageDown (Y),
 *   R/F (rotX), T/G (rotY), Y/H (rotZ), U/J (scale)
 * - Hold Shift for x10 step, Ctrl for x0.1 step
 * - Copy: puts current values as JSON-like into clipboard
 */
public class LanternDebugScreen extends Screen {
    private static final float[] STEP_PRESETS = new float[]{0.005f, 0.01f, 0.025f, 0.05f, 0.1f};
    private int stepIndex = 1;
    private boolean prevDebugEnabled;

    private TextFieldWidget copyPreviewField;

    public LanternDebugScreen() {
        super(Text.literal("Lantern Debug"));
    }

    @Override
    protected void init() {
        prevDebugEnabled = ExampleModFabricClient.isDebugDrawEnabled();
        ExampleModFabricClient.setDebugDrawEnabled(true);

        int left = 20;
        int top = 30;
        int row = 0;
        int colGap = 78;

        // Helper lambdas
        Runnable save = () -> AutoConfig.getConfigHolder(BLClientConfig.class).save();
        BLClientConfig cfg = BLConfigHolder.get();

        // Section: Offset
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset X-"), b -> { cfg.offsetX100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X+"), b -> { cfg.offsetX100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset Y-"), b -> { cfg.offsetY100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Y+"), b -> { cfg.offsetY100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset Z-"), b -> { cfg.offsetZ100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z+"), b -> { cfg.offsetZ100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());

        // Section: Pivot
        row += 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot X-"), b -> { cfg.pivotX100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X+"), b -> { cfg.pivotX100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot Y-"), b -> { cfg.pivotY100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Y+"), b -> { cfg.pivotY100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot Z-"), b -> { cfg.pivotZ100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z+"), b -> { cfg.pivotZ100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());

        // Section: Rotation
        row += 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("RotX -"), b -> { cfg.rotXDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { cfg.rotXDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("RotY -"), b -> { cfg.rotYDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { cfg.rotYDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("RotZ -"), b -> { cfg.rotZDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { cfg.rotZDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());

        // Section: Scale
        row += 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"), b -> { cfg.scale100 = Math.max(1, cfg.scale100 - scaledScaleStep100()); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> { cfg.scale100 += scaledScaleStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());

        // Step / debug / copy / close controls
        int right = this.width - 20 - 100;
        addDrawableChild(ButtonWidget.builder(Text.literal("Step: " + stepText()), b -> { cycleStep(); b.setMessage(Text.literal("Step: " + stepText())); }).dimensions(right, 30, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Axes: ON"), b -> {
            boolean now = !ExampleModFabricClient.isDebugDrawEnabled();
            ExampleModFabricClient.setDebugDrawEnabled(now);
            b.setMessage(Text.literal("Axes: " + (now ? "ON" : "OFF")));
        }).dimensions(right, 54, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Copy values"), b -> { copyValuesToClipboard(); }).dimensions(right, 78, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> { resetDefaults(); save.run(); refreshCopyPreview(); }).dimensions(right, 102, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close()).dimensions(right, 126, 100, 20).build());

        copyPreviewField = new TextFieldWidget(this.textRenderer, right - 40, 160, 140, 20, Text.literal("preview"));
        copyPreviewField.setEditable(false);
        addDrawableChild(copyPreviewField);
        refreshCopyPreview();
    }

    private void resetDefaults() {
        // {offset:[-0,25,-0,05,-0,60], pivot:[0,50,0,60,0,50], rot:[180,0,0], scale:0,50}
        BLClientConfig cfg = BLConfigHolder.get();
        cfg.offsetX100 = -25;
        cfg.offsetY100 = -5;
        cfg.offsetZ100 = -60;
        cfg.pivotX100 = 50;
        cfg.pivotY100 = 60;
        cfg.pivotZ100 = 50;
        cfg.rotXDeg = 180;
        cfg.rotYDeg = 0;
        cfg.rotZDeg = 0;
        cfg.scale100 = 50;
        AutoConfig.getConfigHolder(BLClientConfig.class).save();
    }

    private int scaledStep100() {
        float base = STEP_PRESETS[stepIndex];
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 10.0f;
        if (hasControlDown()) mul *= 0.1f;
        return Math.max(1, Math.round(100f * base * mul));
    }

    private int scaledScaleStep100() {
        // Slightly coarser scale step
        float base = Math.max(0.01f, STEP_PRESETS[stepIndex] * 2.0f);
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 10.0f;
        if (hasControlDown()) mul *= 0.5f;
        return Math.max(1, Math.round(100f * base * mul));
    }

    private int scaledRotStepDeg() {
        float base = 5f; // base 5 degrees
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 3.0f; // 15 deg
        if (hasControlDown()) mul *= 0.2f; // 1 deg
        return Math.max(1, Math.round(base * mul));
    }

    private void cycleStep() {
        stepIndex = (stepIndex + 1) % STEP_PRESETS.length;
    }

    private String stepText() {
        return String.format("%.3f", STEP_PRESETS[stepIndex]);
    }

    private void refreshCopyPreview() {
        BLClientConfig c = BLConfigHolder.get();
        String s = String.format("off(%.2f,%.2f,%.2f) piv(%.2f,%.2f,%.2f) rot(%d,%d,%d) sc(%.2f)",
                c.fOffsetX(), c.fOffsetY(), c.fOffsetZ(), c.fPivotX(), c.fPivotY(), c.fPivotZ(), c.rotXDeg, c.rotYDeg, c.rotZDeg, c.fScale());
        if (copyPreviewField != null) copyPreviewField.setText(s);
    }

    private void copyValuesToClipboard() {
        BLClientConfig c = BLConfigHolder.get();
        String jsonish = String.format("{offset:[%.2f,%.2f,%.2f], pivot:[%.2f,%.2f,%.2f], rot:[%d,%d,%d], scale:%.2f}",
                c.fOffsetX(), c.fOffsetY(), c.fOffsetZ(), c.fPivotX(), c.fPivotY(), c.fPivotZ(), c.rotXDeg, c.rotYDeg, c.rotZDeg, c.fScale());
        MinecraftClient.getInstance().keyboard.setClipboard(jsonish);
    }

    @Override
    public void close() {
        ExampleModFabricClient.setDebugDrawEnabled(prevDebugEnabled);
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Nudge via keyboard
        BLClientConfig cfg = BLConfigHolder.get();
        boolean used = false;
        switch (keyCode) {
            case 262: cfg.offsetX100 += scaledStep100(); used = true; break; // Right
            case 263: cfg.offsetX100 -= scaledStep100(); used = true; break; // Left
            case 265: cfg.offsetZ100 -= scaledStep100(); used = true; break; // Up (towards -Z)
            case 264: cfg.offsetZ100 += scaledStep100(); used = true; break; // Down (+Z)
            case 266: cfg.offsetY100 += scaledStep100(); used = true; break; // PageUp
            case 267: cfg.offsetY100 -= scaledStep100(); used = true; break; // PageDown
            // Rotations: R/F, T/G, Y/H
            case 82: cfg.rotXDeg += scaledRotStepDeg(); used = true; break; // R
            case 70: cfg.rotXDeg -= scaledRotStepDeg(); used = true; break; // F
            case 84: cfg.rotYDeg += scaledRotStepDeg(); used = true; break; // T
            case 71: cfg.rotYDeg -= scaledRotStepDeg(); used = true; break; // G
            case 89: cfg.rotZDeg += scaledRotStepDeg(); used = true; break; // Y
            case 72: cfg.rotZDeg -= scaledRotStepDeg(); used = true; break; // H
            // Pivot: I/K, O/L, P/;  (ASCII: I=73,K=75,O=79,L=76,P=80,;=59)
            case 73: cfg.pivotX100 += scaledStep100(); used = true; break; // I -> +X
            case 75: cfg.pivotX100 -= scaledStep100(); used = true; break; // K -> -X
            case 79: cfg.pivotY100 += scaledStep100(); used = true; break; // O -> +Y
            case 76: cfg.pivotY100 -= scaledStep100(); used = true; break; // L -> -Y
            case 80: cfg.pivotZ100 += scaledStep100(); used = true; break; // P -> +Z
            case 59: cfg.pivotZ100 -= scaledStep100(); used = true; break; // ; -> -Z
            // Scale: U/J
            case 85: cfg.scale100 += scaledScaleStep100(); used = true; break; // U
            case 74: cfg.scale100 = Math.max(1, cfg.scale100 - scaledScaleStep100()); used = true; break; // J
            default: break;
        }
        if (used) {
            AutoConfig.getConfigHolder(BLClientConfig.class).save();
            refreshCopyPreview();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void applyBlur(float delta){

    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // No background dim/blur: keep world fully visible
        super.render(ctx, mouseX, mouseY, delta);
        BLClientConfig c = BLConfigHolder.get();
        int x = 20;
        int y = 10;
        ctx.drawText(this.textRenderer, Text.literal("Lantern Debug (hold Shift=×10, Ctrl=×0.1)" ).formatted(Formatting.YELLOW), x, y, 0xFFFFFF, false);
        y += 14;
        ctx.drawText(this.textRenderer, Text.literal(String.format("Offset: X=%.2f Y=%.2f Z=%.2f", c.fOffsetX(), c.fOffsetY(), c.fOffsetZ())), x, y, 0xFFFFFF, false);
        y += 12;
        ctx.drawText(this.textRenderer, Text.literal(String.format("Pivot:  X=%.2f Y=%.2f Z=%.2f", c.fPivotX(), c.fPivotY(), c.fPivotZ())), x, y, 0xFFFFFF, false);
        y += 12;
        ctx.drawText(this.textRenderer, Text.literal(String.format("Rot:    X=%d Y=%d Z=%d", c.rotXDeg, c.rotYDeg, c.rotZDeg)), x, y, 0xFFFFFF, false);
        y += 12;
        ctx.drawText(this.textRenderer, Text.literal(String.format("Scale:  %.2f", c.fScale())), x, y, 0xFFFFFF, false);
    }

    @Override
    public boolean shouldPause() {
        // Do not pause world; avoids some pause-related shaders/blur in some setups
        return false;
    }
}
