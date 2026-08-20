package net.tablesouls.souls_combat_hud.client.util.animation;

import net.minecraft.util.Mth;

public class ValueAnimator {

    public interface Easing {
        float apply(float t);

        Easing LINEAR = t -> t;
        Easing EASE_OUT_QUAD = t -> 1f - (1f - t) * (1f - t);
        Easing EASE_IN_QUAD = t -> t * t;
        Easing EASE_OUT_BACK = t -> {
            float c1 = 1.70158f;
            float c3 = c1 + 1f;
            float tm1 = t - 1f;
            return 1f + c3 * tm1 * tm1 * tm1 + c1 * tm1 * tm1;
        };
    }

    private final long durationMs;
    private final long delayMs;
    private final Easing easing;

    private float start;
    private float target;
    private float current;
    private long triggerMillis;
    private boolean initialized = false;

    public ValueAnimator(long durationMs, long delayMs, Easing easing) {
        this.durationMs = Math.max(1L, durationMs);
        this.delayMs = Math.max(0L, delayMs);
        this.easing = easing;
    }

    public float update(float newTarget) {
        long now = System.currentTimeMillis();

        if (!initialized) {
            this.start = this.target = this.current = newTarget;
            this.initialized = true;
            return current;
        }

        if (newTarget != target) {
            this.start = current;
            this.target = newTarget;
            this.triggerMillis = now;
        }

        long elapsed = now - triggerMillis;
        if (elapsed <= delayMs) {
            current = start;
        } else {
            float t = Mth.clamp((elapsed - delayMs) / (float) durationMs, 0.0f, 1.0f);
            current = Mth.lerp(easing.apply(t), start, target);
        }
        return current;
    }

    public void snapTo(float value) {
        this.start = this.target = this.current = value;
        this.initialized = true;
        this.triggerMillis = System.currentTimeMillis();
    }

    public float getCurrent() {
        return current;
    }
}