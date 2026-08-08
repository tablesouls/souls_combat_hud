package net.tablesouls.souls_combat_hud.client.util.animation;

public class FadeAnimator {

    private final ValueAnimator fadeIn;
    private final ValueAnimator fadeOut;
    private boolean targetVisible = false;
    private Boolean activeDirectionVisible = null;

    public FadeAnimator(long fadeInMillis, long fadeOutMillis) {
        this(fadeInMillis, fadeOutMillis, 0L);
    }

    public FadeAnimator(long fadeInMillis, long fadeOutMillis, long fadeOutHoldMillis) {
        this.fadeIn = new ValueAnimator(fadeInMillis, 0L, ValueAnimator.Easing.LINEAR);
        this.fadeOut = new ValueAnimator(fadeOutMillis, fadeOutHoldMillis, ValueAnimator.Easing.LINEAR);
    }

    public void setVisible(boolean visible) {
        this.targetVisible = visible;
    }

    public float tick() {
        if (activeDirectionVisible == null || activeDirectionVisible != targetVisible) {
            float handoff = activeDirectionVisible == null
                    ? (targetVisible ? 0.0f : 1.0f)
                    : (targetVisible ? fadeOut.getCurrent() : fadeIn.getCurrent());

            (targetVisible ? fadeIn : fadeOut).snapTo(handoff);
            activeDirectionVisible = targetVisible;
        }

        ValueAnimator active = targetVisible ? fadeIn : fadeOut;
        return active.update(targetVisible ? 1.0f : 0.0f);
    }

    public float getAlpha() {
        if (activeDirectionVisible == null) return 0.0f;
        return (activeDirectionVisible ? fadeIn : fadeOut).getCurrent();
    }

    public boolean isHidden() {
        return getAlpha() <= 0.0f && !targetVisible;
    }
}