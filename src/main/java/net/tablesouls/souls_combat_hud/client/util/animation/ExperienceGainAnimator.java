package net.tablesouls.souls_combat_hud.client.util.animation;

import net.minecraft.util.Mth;

public class ExperienceGainAnimator {

    public enum Phase {
        IDLE,
        POP_IN,
        HOLD,
        SLIDE_OUT
    }

    private static final long POP_IN_MS = 180L;
    private static final long BASE_HOLD_MS = 700L;
    private static final long HOLD_PER_GAIN_MS = 150L;
    private static final long MAX_HOLD_MS = 2500L;
    private static final long SLIDE_OUT_MS = 350L;
    private static final long COUNT_MS = 500L;

    private final ValueAnimator popAnimator = new ValueAnimator(POP_IN_MS, 0L, ValueAnimator.Easing.EASE_OUT_BACK);
    private final ValueAnimator slideAnimator = new ValueAnimator(SLIDE_OUT_MS, 0L, ValueAnimator.Easing.EASE_IN_QUAD);
    private final ValueAnimator counterAnimator = new ValueAnimator(COUNT_MS, 0L, ValueAnimator.Easing.EASE_OUT_QUAD);

    private Phase phase = Phase.IDLE;
    private long phaseStartMillis = 0L;
    private long holdDurationMillis = BASE_HOLD_MS;

    private int lastKnownTotal = -1;
    private int pendingGain = 0;
    private int counterBaseValue = 0;
    private int counterTargetValue = 0;

    public void update(int currentTotal) {
        update(currentTotal, true);
    }

    public void update(int currentTotal, boolean popupEnabled) {
        if (!popupEnabled) {
            reset(currentTotal);
            lastKnownTotal = currentTotal;
            return;
        }

        long now = System.currentTimeMillis();

        if (lastKnownTotal < 0) {
            lastKnownTotal = currentTotal;
            counterBaseValue = currentTotal;
            counterTargetValue = currentTotal;
            counterAnimator.snapTo(currentTotal);
            return;
        }

        int delta = currentTotal - lastKnownTotal;
        if (delta > 0) {
            onGain(delta, now);
        } else if (delta < 0) {
            reset(currentTotal);
        }
        lastKnownTotal = currentTotal;

        tickPhase(now);
    }

    private void onGain(int amount, long now) {
        pendingGain += amount;
        counterTargetValue += amount;

        switch (phase) {
            case IDLE:
                phase = Phase.POP_IN;
                phaseStartMillis = now;
                holdDurationMillis = BASE_HOLD_MS;
                popAnimator.snapTo(0f);
                break;
            case SLIDE_OUT:
                phase = Phase.POP_IN;
                phaseStartMillis = now;
                holdDurationMillis = BASE_HOLD_MS;
                popAnimator.snapTo(1f - slideAnimator.getCurrent());
                break;
            case POP_IN:
            case HOLD:
                holdDurationMillis = Math.min(MAX_HOLD_MS, holdDurationMillis + HOLD_PER_GAIN_MS);
                break;
        }
    }

    private void tickPhase(long now) {
        switch (phase) {
            case POP_IN:
                popAnimator.update(1f);
                if (now - phaseStartMillis >= POP_IN_MS) {
                    phase = Phase.HOLD;
                    phaseStartMillis = now;
                }
                break;
            case HOLD:
                if (now - phaseStartMillis >= holdDurationMillis) {
                    phase = Phase.SLIDE_OUT;
                    phaseStartMillis = now;
                    slideAnimator.snapTo(0f);
                    counterAnimator.snapTo(counterBaseValue);
                }
                break;
            case SLIDE_OUT:
                slideAnimator.update(1f);
                counterAnimator.update(counterTargetValue);
                if (now - phaseStartMillis >= SLIDE_OUT_MS) {
                    phase = Phase.IDLE;
                    pendingGain = 0;
                    counterBaseValue = counterTargetValue;
                    counterAnimator.snapTo(counterTargetValue);
                }
                break;
            case IDLE:
                counterAnimator.update(counterTargetValue);
                counterBaseValue = counterTargetValue;
                break;
        }
    }

    private void reset(int currentTotal) {
        phase = Phase.IDLE;
        pendingGain = 0;
        counterBaseValue = currentTotal;
        counterTargetValue = currentTotal;
        counterAnimator.snapTo(currentTotal);
        popAnimator.snapTo(0f);
        slideAnimator.snapTo(0f);
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isPopupVisible() {
        return phase != Phase.IDLE;
    }

    public int getPendingGain() {
        return pendingGain;
    }

    public float getPopupScale() {
        if (phase == Phase.POP_IN) {
            return popAnimator.getCurrent();
        }
        return 1.0f;
    }

    public float getPopupAlpha() {
        switch (phase) {
            case POP_IN:
                return Mth.clamp(popAnimator.getCurrent(), 0f, 1f);
            case HOLD:
                return 1.0f;
            case SLIDE_OUT:
                return 1.0f - Mth.clamp(slideAnimator.getCurrent(), 0f, 1f);
            default:
                return 0.0f;
        }
    }

    public float getPopupOffsetY(float slideDistancePx) {
        return phase == Phase.SLIDE_OUT ? slideAnimator.getCurrent() * slideDistancePx : 0f;
    }

    public int getDisplayedTotal() {
        return Math.round(counterAnimator.getCurrent());
    }
}