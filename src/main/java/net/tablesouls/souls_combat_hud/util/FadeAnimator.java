package net.tablesouls.souls_combat_hud.util;

public class FadeAnimator {
    private static final long MAX_DELTA_MS = 50L; // caps how much a single tick can move, even after a lag spike

    private final float fadeInPerMs;
    private final float fadeOutPerMs;
    private boolean targetVisible = false;
    private float alpha = 0.0f;
    private long lastUpdateMillis = System.currentTimeMillis();

    public FadeAnimator(long fadeInMillis, long fadeOutMillis) {
        this.fadeInPerMs = fadeInMillis <= 0L ? Float.MAX_VALUE : 1.0f / (float) fadeInMillis;
        this.fadeOutPerMs = fadeOutMillis <= 0L ? Float.MAX_VALUE : 1.0f / (float) fadeOutMillis;
    }

    public void setVisible(boolean visible) {
        this.targetVisible = visible;
    }

    public float tick() {
        long now = System.currentTimeMillis();
        long delta = Math.min(MAX_DELTA_MS, Math.max(0L, now - this.lastUpdateMillis));
        this.lastUpdateMillis = now;
        float step = (this.targetVisible ? this.fadeInPerMs : -this.fadeOutPerMs) * (float) delta;
        this.alpha = Math.max(0.0f, Math.min(1.0f, this.alpha + step));
        return this.alpha;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public boolean isHidden() {
        return this.alpha <= 0.0f && !this.targetVisible;
    }
}