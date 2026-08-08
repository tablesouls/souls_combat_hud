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

    private float lastFraction = -1f;
    private float lastRawValue = -1f;
    private int pendingDamageRaw = 0;
    private long damageTextExpireAt = 0L;
    private static final long DAMAGE_TEXT_HOLD_MS = 1500L;

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
        return withFade(fadeInMs, fadeOutMs, 0L);
    }

    public static BarElement withFade(long fadeInMs, long fadeOutMs, long fadeOutHoldMs) {
        return new BarElement(new FadeAnimator(fadeInMs, fadeOutMs, fadeOutHoldMs));
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

    private void recordDamage(int rawDamage) {
        if (rawDamage <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < damageTextExpireAt) {
            pendingDamageRaw += rawDamage;
        } else {
            pendingDamageRaw = rawDamage;
        }
        damageTextExpireAt = now + DAMAGE_TEXT_HOLD_MS;
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
        render(graphics, style, x, y, w, h, currentFraction, label, valueText, mirrored, reductionEnabled, tint, maxValue, -1f);
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, Component valueText, boolean mirrored, boolean reductionEnabled,
            float tint, float maxValue, float currentRawValue
    ) {
        render(graphics, style, x, y, w, h, currentFraction, label, valueText, mirrored, reductionEnabled, tint, maxValue, currentRawValue, true);
    }

    public void render(
            GuiGraphics graphics, BarStyle style, int x, int y, int w, int h,
            float currentFraction, Component label, Component valueText, boolean mirrored, boolean reductionEnabled,
            float tint, float maxValue, float currentRawValue, boolean showDamageText
    ) {
        if (isHidden()) {
            return;
        }

        float alpha = getAlpha();
        float displayedFraction = getDisplayedFraction(currentFraction, maxValue);

        if (currentRawValue >= 0f) {
            if (lastRawValue < 0f) {
                lastRawValue = currentRawValue;
            } else if (currentRawValue < lastRawValue - 0.001f) {
                recordDamage(Math.round(lastRawValue - currentRawValue));
                lastRawValue = currentRawValue;
            } else if (currentRawValue > lastRawValue + 0.001f) {
                lastRawValue = currentRawValue;
            }
        } else if (lastFraction < 0f) {
            lastFraction = currentFraction;
        } else if (currentFraction < lastFraction - 0.0001f && maxValue > 0f) {
            float dropFraction = lastFraction - currentFraction;
            recordDamage(Math.round(dropFraction * maxValue));
            lastFraction = currentFraction;
        } else if (currentFraction > lastFraction + 0.0001f) {
            lastFraction = currentFraction;
        }

        Component damageText = null;
        if (reductionEnabled && System.currentTimeMillis() < damageTextExpireAt) {
            damageText = Component.literal(String.valueOf(pendingDamageRaw));
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(tint, tint, tint, alpha);

        BarRenderer.render(graphics, style, x, y, w, h, currentFraction, displayedFraction, label, valueText, damageText, mirrored, reductionEnabled, showDamageText);

        if (decoration != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BarDecorationRenderer.render(graphics, decoration, x, y, w, h, currentFraction, mirrored);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}