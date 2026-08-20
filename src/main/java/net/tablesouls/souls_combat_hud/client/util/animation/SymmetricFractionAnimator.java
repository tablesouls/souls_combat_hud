package net.tablesouls.souls_combat_hud.client.util.animation;

public class SymmetricFractionAnimator {

    private final ValueAnimator animator;
    private boolean initialized = false;

    public SymmetricFractionAnimator(long durationMs) {
        this.animator = new ValueAnimator(durationMs, 0L, ValueAnimator.Easing.LINEAR);
    }

    public float update(float currentValue) {
        if (!initialized) {
            animator.snapTo(currentValue);
            initialized = true;
            return currentValue;
        }
        return animator.update(currentValue);
    }

    public float getCurrent() {
        return animator.getCurrent();
    }

    public void reset() {
        initialized = false;
    }
}