package net.tablesouls.souls_combat_hud.client.util.animation;

public class IncreaseSmoothAnimator {
    private static final float RISE_PIXELS_PER_MS = 0.08f;
    private static final float SNAP_EPSILON = 1.0E-3f;

    private float current = -1f;
    private float lastKnownValue = -1f;
    private float lastKnownMax = -1f;
    private long lastUpdateMillis;
    private boolean initialized = false;

    public float update(float currentValue) {
        return update(currentValue, -1f, -1f);
    }

    public float update(float currentValue, float maxValue) {
        return update(currentValue, maxValue, -1f);
    }

    public float update(float currentValue, float maxValue, float widthPx) {
        long now = System.currentTimeMillis();

        boolean maxChanged = initialized
                && maxValue >= 0f
                && lastKnownMax >= 0f
                && Math.abs(maxValue - lastKnownMax) > 1.0E-4f;

        if (!initialized) {
            current = currentValue;
            lastKnownValue = currentValue;
            lastKnownMax = maxValue;
            lastUpdateMillis = now;
            initialized = true;
            return current;
        }

        long dt = Math.max(0L, now - lastUpdateMillis);
        lastUpdateMillis = now;

        if (maxChanged || currentValue < lastKnownValue - 1.0E-4f) {
            current = currentValue;
        } else if (currentValue - current > SNAP_EPSILON) {
            float ratePerMs = widthPx > 0f
                    ? RISE_PIXELS_PER_MS / widthPx
                    : (1.0f / 1000f);
            current += ratePerMs * dt;
            if (current > currentValue) {
                current = currentValue;
            }
        } else {
            current = currentValue;
        }

        lastKnownValue = currentValue;
        lastKnownMax = maxValue;
        return current;
    }
}