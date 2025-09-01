package net.oxcodsnet.beltborne_lanterns.common.client.ui;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.oxcodsnet.beltborne_lanterns.common.client.BLClientAbstractions;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfig;
import net.oxcodsnet.beltborne_lanterns.common.config.BLClientConfigAccess;

/**
 * Simple in-game debug UI to tweak lantern FeatureRenderer transforms.
 * Loader-agnostic: relies only on Minecraft client + AutoConfig.
 */
public class LanternDebugScreen extends Screen {
    private static final float[] STEP_PRESETS = new float[]{0.005f, 0.01f, 0.025f, 0.05f, 0.1f};
    private int stepIndex = 1;
    private boolean prevDebugEnabled;

    private TextFieldWidget copyPreviewField;
    private ButtonWidget stepButton;

    public LanternDebugScreen() {
        super(Text.literal("Lantern Debug"));
    }

    @Override
    protected void init() {
        prevDebugEnabled = BLClientAbstractions.isDebugDrawEnabled();
        BLClientAbstractions.setDebugDrawEnabled(true);

        int left = 20;
        int top = 80; // push controls below the header text
        int row = 0;
        int colGap = 78;

        // Right column for scale/step/copy to save horizontal space
        int rightColWidth = 240;
        int rightX = this.width - 20 - rightColWidth;
        int rightRow = 0;

        // Helper lambdas
        Runnable save = BLClientConfigAccess::save;
        BLClientConfig cfg = BLClientConfigAccess.get();

        // Section: Offset
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset X-"), b -> { cfg.offsetX100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X+"), b -> { cfg.offsetX100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset Y-"), b -> { cfg.offsetY100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Y+"), b -> { cfg.offsetY100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Offset Z-"), b -> { cfg.offsetZ100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z+"), b -> { cfg.offsetZ100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;

        // Section: Pivot
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot X-"), b -> { cfg.pivotX100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X+"), b -> { cfg.pivotX100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot Y-"), b -> { cfg.pivotY100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Y+"), b -> { cfg.pivotY100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Pivot Z-"), b -> { cfg.pivotZ100 -= scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z+"), b -> { cfg.pivotZ100 += scaledStep100(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;

        // Section: Rotation
        addDrawableChild(ButtonWidget.builder(Text.literal("Rot X-"), b -> { cfg.rotXDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("X+"), b -> { cfg.rotXDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Rot Y-"), b -> { cfg.rotYDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Y+"), b -> { cfg.rotYDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;
        addDrawableChild(ButtonWidget.builder(Text.literal("Rot Z-"), b -> { cfg.rotZDeg -= scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left, top + row * 22, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Z+"), b -> { cfg.rotZDeg += scaledRotStepDeg(); save.run(); refreshCopyPreview(); }).dimensions(left + colGap, top + row * 22, 50, 20).build());
        row++;

        // Section (right): Scale + Step
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"), b -> {
            cfg.scale100 = Math.max(1, cfg.scale100 - scaledScaleStep100());
            save.run();
            refreshCopyPreview();
        }).dimensions(rightX, top + rightRow * 22, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"), b -> {
            cfg.scale100 += scaledScaleStep100();
            save.run();
            refreshCopyPreview();
        }).dimensions(rightX + 74, top + rightRow * 22, 70, 20).build());
        stepButton = addDrawableChild(ButtonWidget.builder(Text.literal("Step: " + stepText()), b -> {
            cycleStep();
            stepButton.setMessage(Text.literal("Step: " + stepText()));
        }).dimensions(rightX + 148, top + rightRow * 22, 90, 20).build());
        rightRow++;

        // Section (right): Copy preview & button
        int previewWidth = 180;
        copyPreviewField = new TextFieldWidget(this.textRenderer, rightX, top + rightRow * 22, previewWidth, 20, Text.literal("CopyPreview"));
        copyPreviewField.setEditable(false);
        addDrawableChild(copyPreviewField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Copy"), b -> copyValuesToClipboard())
                .dimensions(rightX + previewWidth + 4, top + rightRow * 22, rightColWidth - previewWidth - 4, 20)
                .build());
        refreshCopyPreview();
    }

    private int scaledStep100() {
        float base = STEP_PRESETS[stepIndex] * 100f;
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 10f;
        if (hasControlDown()) mul *= 0.1f;
        return Math.max(1, Math.round(base * mul));
    }

    private int scaledScaleStep100() {
        float base = 5f; // 0.05
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 10f;
        if (hasControlDown()) mul *= 0.1f;
        return Math.max(1, Math.round(base * mul));
    }

    private int scaledRotStepDeg() {
        float base = 5f;
        float mul = 1.0f;
        if (hasShiftDown()) mul *= 3.0f;
        if (hasControlDown()) mul *= 0.2f;
        return Math.max(1, Math.round(base * mul));
    }

    private void cycleStep() {
        stepIndex = (stepIndex + 1) % STEP_PRESETS.length;
    }

    private String stepText() {
        return String.format("%.3f", STEP_PRESETS[stepIndex]);
    }

    private void refreshCopyPreview() {
        BLClientConfig c = BLClientConfigAccess.get();
        String s = String.format("off(%.2f,%.2f,%.2f) piv(%.2f,%.2f,%.2f) rot(%d,%d,%d) sc(%.2f)",
                c.fOffsetX(), c.fOffsetY(), c.fOffsetZ(), c.fPivotX(), c.fPivotY(), c.fPivotZ(), c.rotXDeg, c.rotYDeg, c.rotZDeg, c.fScale());
        if (copyPreviewField != null) copyPreviewField.setText(s);
    }

    private void copyValuesToClipboard() {
        BLClientConfig c = BLClientConfigAccess.get();
        String jsonish = String.format("{offset:[%.2f,%.2f,%.2f], pivot:[%.2f,%.2f,%.2f], rot:[%d,%d,%d], scale:%.2f}",
                c.fOffsetX(), c.fOffsetY(), c.fOffsetZ(), c.fPivotX(), c.fPivotY(), c.fPivotZ(), c.rotXDeg, c.rotYDeg, c.rotZDeg, c.fScale());
        MinecraftClient.getInstance().keyboard.setClipboard(jsonish);
    }

    @Override
    public void close() {
        BLClientAbstractions.setDebugDrawEnabled(prevDebugEnabled);
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        BLClientConfig cfg = BLClientConfigAccess.get();
        boolean used = false;
        switch (keyCode) {
            case 262: cfg.offsetX100 += scaledStep100(); used = true; break;
            case 263: cfg.offsetX100 -= scaledStep100(); used = true; break;
            case 265: cfg.offsetZ100 -= scaledStep100(); used = true; break;
            case 264: cfg.offsetZ100 += scaledStep100(); used = true; break;
            case 266: cfg.offsetY100 += scaledStep100(); used = true; break;
            case 267: cfg.offsetY100 -= scaledStep100(); used = true; break;
            case 82: cfg.rotXDeg += scaledRotStepDeg(); used = true; break;
            case 70: cfg.rotXDeg -= scaledRotStepDeg(); used = true; break;
            case 84: cfg.rotYDeg += scaledRotStepDeg(); used = true; break;
            case 71: cfg.rotYDeg -= scaledRotStepDeg(); used = true; break;
            case 89: cfg.rotZDeg += scaledRotStepDeg(); used = true; break;
            case 72: cfg.rotZDeg -= scaledRotStepDeg(); used = true; break;
            case 73: cfg.pivotX100 += scaledStep100(); used = true; break;
            case 75: cfg.pivotX100 -= scaledStep100(); used = true; break;
            case 79: cfg.pivotY100 += scaledStep100(); used = true; break;
            case 76: cfg.pivotY100 -= scaledStep100(); used = true; break;
            case 80: cfg.pivotZ100 += scaledStep100(); used = true; break;
            case 59: cfg.pivotZ100 -= scaledStep100(); used = true; break;
            case 85: cfg.scale100 += scaledScaleStep100(); used = true; break;
            case 74: cfg.scale100 = Math.max(1, cfg.scale100 - scaledScaleStep100()); used = true; break;
            default: break;
        }
        if (used) {
            BLClientConfigAccess.save();
            refreshCopyPreview();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void applyBlur() {
        // keep world visible
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        BLClientConfig c = BLClientConfigAccess.get();
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
        return false;
    }
}
