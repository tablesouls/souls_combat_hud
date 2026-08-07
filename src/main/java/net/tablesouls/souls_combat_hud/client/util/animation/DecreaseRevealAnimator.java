package net.tablesouls.souls_combat_hud.client.util.animation;

public class DecreaseRevealAnimator {
    private static final long HOLD_MS = 400L;
    private static final long DRAIN_MS = 500L;

    private final ValueAnimator animator = new ValueAnimator(DRAIN_MS, HOLD_MS, ValueAnimator.Easing.EASE_OUT_QUAD);
    private float lastKnownValue = 1.0f;
    private float lastKnownMax = -1.0f;
    private boolean initialized = false;

    public float update(float currentValue) {
        return update(currentValue, -1.0f);
    }

    public float update(float currentValue, float maxValue) {
        boolean maxChanged = initialized
                && maxValue >= 0f
                && lastKnownMax >= 0f
                && Math.abs(maxValue - lastKnownMax) > 1.0E-4f;

        if (!initialized) {
            animator.snapTo(currentValue);
            lastKnownValue = currentValue;
            lastKnownMax = maxValue;
            initialized = true;
            return currentValue;
        }

        if (maxChanged || currentValue > lastKnownValue + 1.0E-4f) {
            animator.snapTo(currentValue);
        }

        lastKnownValue = currentValue;
        lastKnownMax = maxValue;
        return animator.update(currentValue);
    }
}