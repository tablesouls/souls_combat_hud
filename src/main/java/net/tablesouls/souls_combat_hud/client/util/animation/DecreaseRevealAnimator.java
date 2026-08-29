package net.tablesouls.souls_combat_hud.client.util.animation;

public class DecreaseRevealAnimator {
    private static final long HOLD_MS = 1000L;
    private static final float DRAIN_PIXELS_PER_MS = 0.1f;

    private float lastKnownValue = 1.0f;
    private float lastKnownMax = -1.0f;
    private boolean initialized = false;

    private float lastDisplayedValue = 1.0f;

    private boolean sequenceActive = false;
    private long holdStartMillis;
    private float holdValue;

    private boolean draining = false;
    private float drainCurrent;
    private long lastDrainTickMillis;
    private float sequenceBottom;

    public float update(float currentValue) {
        return update(currentValue, -1.0f, -1.0f);
    }

    public float update(float currentValue, float maxValue) {
        return update(currentValue, maxValue, -1.0f);
    }

    public float update(float currentValue, float maxValue, float widthPx) {
        long now = System.currentTimeMillis();

        boolean maxChanged = initialized
                && maxValue >= 0f
                && lastKnownMax >= 0f
                && Math.abs(maxValue - lastKnownMax) > 1.0E-4f;

        if (!initialized) {
            lastKnownValue = currentValue;
            lastKnownMax = maxValue;
            lastDisplayedValue = currentValue;
            initialized = true;
            sequenceActive = false;
            draining = false;
            return currentValue;
        }

        if (maxChanged) {
            lastKnownValue = currentValue;
            lastKnownMax = maxValue;
            lastDisplayedValue = currentValue;
            sequenceActive = false;
            draining = false;
            return currentValue;
        }

        if (currentValue < lastKnownValue - 1.0E-4f) {
            if (!sequenceActive) {
                holdStartMillis = now;
                holdValue = lastDisplayedValue;
                sequenceActive = true;
                draining = false;
            }
            sequenceBottom = currentValue;
            if (draining) {
                drainCurrent = lastDisplayedValue;
                lastDrainTickMillis = now;
            }
        }

        lastKnownValue = currentValue;
        lastKnownMax = maxValue;

        float result;
        if (!sequenceActive) {
            result = currentValue;
        } else {
            long elapsed = now - holdStartMillis;
            if (elapsed <= HOLD_MS) {
                result = holdValue;
            } else {
                if (!draining) {
                    draining = true;
                    drainCurrent = holdValue;
                    lastDrainTickMillis = now;
                }
                long dt = Math.max(0L, now - lastDrainTickMillis);
                lastDrainTickMillis = now;

                float ratePerMs = widthPx > 0f
                        ? DRAIN_PIXELS_PER_MS / widthPx
                        : (1.0f / 1000f);

                drainCurrent -= ratePerMs * dt;
                if (drainCurrent <= sequenceBottom + 1.0E-4f) {
                    drainCurrent = sequenceBottom;
                    sequenceActive = false;
                    draining = false;
                }
                result = drainCurrent;
            }
        }

        lastDisplayedValue = result;
        return result;
    }
}