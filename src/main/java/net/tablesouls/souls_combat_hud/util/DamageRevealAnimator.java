package net.tablesouls.souls_combat_hud.util;

public class DamageRevealAnimator {
    private static final long HOLD_MS = 400L;
    private static final long DRAIN_MS = 500L;

    private float lastKnownProgress = 1.0f;
    private float revealStart = 1.0f;
    private float revealTarget = 1.0f;
    private long revealTriggerMillis = 0L;
    private boolean initialized = false;

    public float update(float currentProgress) {
        long now = System.currentTimeMillis();
        if (!this.initialized) {
            this.revealStart = this.revealTarget = currentProgress;
            this.lastKnownProgress = this.revealTarget;
            this.initialized = true;
            return currentProgress;
        }
        if (currentProgress > this.lastKnownProgress + 1.0E-4f) {
            this.revealStart = this.revealTarget = currentProgress;
            this.revealTriggerMillis = now;
        } else if (currentProgress < this.lastKnownProgress - 1.0E-4f) {
            float displayedNow;
            this.revealStart = displayedNow = this.computeDisplayed(now);
            this.revealTarget = currentProgress;
            this.revealTriggerMillis = now;
        }
        this.lastKnownProgress = currentProgress;
        return this.computeDisplayed(now);
    }

    private float computeDisplayed(long now) {
        long elapsed = now - this.revealTriggerMillis;
        if (elapsed <= HOLD_MS) {
            return this.revealStart;
        }
        float t = Math.min(1.0f, (float) (elapsed - HOLD_MS) / (float) DRAIN_MS);
        float eased = 1.0f - (1.0f - t) * (1.0f - t);
        return this.revealStart + (this.revealTarget - this.revealStart) * eased;
    }
}