package net.tablesouls.souls_combat_hud.client.render.bars;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tablesouls.souls_combat_hud.client.util.animation.DecreaseRevealAnimator;
import net.tablesouls.souls_combat_hud.client.util.animation.FadeAnimator;

public class BarElement {
    private final DecreaseRevealAnimator reveal = new DecreaseRevealAnimator();
    private final FadeAnimator fade;
    private BarDecoration decoration; // null = no decoration

    public float getAlpha() {
        return fade != null ? fade.tick() : 1.0F;
    }

    public float getDisplayedFraction(float currentFraction) {
        return reveal.update(currentFraction);
    }

    public float getDisplayedFraction(float currentFraction, float maxValue) {
        return reveal.update(currentFraction, maxValue);
    }

    public BarElement() {
        this(null);
    }

    private BarElement(FadeAnimator fade) {
        this.fade = fade;
    }

    public static BarElement alwaysVisible() {
        return new BarElement();
    }

    public static BarElement withFade(long fadeInMs, long fadeOutMs) {
        return new BarElement(new FadeAnimator(fadeInMs, fadeOutMs));
    }

    public BarElement withDecoration(BarDecoration decoration) {
        this.decoration = decoration;
        return this;
    }

    public void setVisible(boolean visible) {
        if (fade != null) {
            fade.setVisible(visible);
        }
    }

    public boolean isHidden() {
        return fade != null && fade.isHidden();
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, boolean mirrored, boolean reductionEnabled
    ) {
        render(graphics, style, x, y, w, h, currentFraction, label, null, mirrored, reductionEnabled);
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, Component valueText, boolean mirrored, boolean reductionEnabled
    ) {
        render(graphics, style, x, y, w, h, currentFraction, label, valueText, mirrored, reductionEnabled, 1.0f);
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, Component valueText, boolean mirrored, boolean reductionEnabled,
            float tint
    ) {
        render(graphics, style, x, y, w, h, currentFraction, label, valueText, mirrored, reductionEnabled, tint, -1f);
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, Component valueText, boolean mirrored, boolean reductionEnabled,
            float tint, float maxValue
    ) {
        if (isHidden()) {
            return;
        }

        float alpha = getAlpha();
        float displayedFraction = getDisplayedFraction(currentFraction, maxValue);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(tint, tint, tint, alpha);

        BarRenderer.render(graphics, style, x, y, w, h, currentFraction, displayedFraction, label, valueText, mirrored, reductionEnabled);

        if (decoration != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BarDecorationRenderer.render(graphics, decoration, x, y, w, h, currentFraction, mirrored);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}