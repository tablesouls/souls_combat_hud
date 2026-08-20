package net.tablesouls.souls_combat_hud.client.util.animation;

public class IncreaseSmoothAnimator {
    private static final long RISE_MS = 400L;

    private final ValueAnimator animator = new ValueAnimator(RISE_MS, 0L, ValueAnimator.Easing.LINEAR);
    private float lastKnownValue = -1f;
    private float lastKnownMax = -1f;
    private boolean initialized = false;

    public float update(float currentValue) {
        return update(currentValue, -1f);
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

        // Snap instantly on decreases (damage) and whenever max changes,
        // only animate when the value is rising.
        if (maxChanged || currentValue < lastKnownValue - 1.0E-4f) {
            animator.snapTo(currentValue);
        }

        lastKnownValue = currentValue;
        lastKnownMax = maxValue;
        return animator.update(currentValue);
    }
}