package net.tablesouls.souls_combat_hud.client.util.animation;

public class DecreaseRevealAnimator {
    private static final long HOLD_MS = 400L;
    private static final long DRAIN_MS = 500L;

    private final ValueAnimator animator = new ValueAnimator(DRAIN_MS, HOLD_MS, ValueAnimator.Easing.EASE_OUT_QUAD);
    private float lastKnownValue = 1.0f;
    private boolean initialized = false;

    public float update(float currentValue) {
        if (!initialized) {
            animator.snapTo(currentValue);
            lastKnownValue = currentValue;
            initialized = true;
            return currentValue;
        }

        if (currentValue > lastKnownValue + 1.0E-4f) {
            animator.snapTo(currentValue);
        }

        lastKnownValue = currentValue;
        return animator.update(currentValue);
    }
}