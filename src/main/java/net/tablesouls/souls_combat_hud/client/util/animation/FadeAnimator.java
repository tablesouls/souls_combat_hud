package net.tablesouls.souls_combat_hud.client.util.animation;

public class FadeAnimator {
    public enum Mode {
        BOTH,
        FADE_IN,
        FADE_OUT
    }

    private final ValueAnimator fadeIn;
    private final ValueAnimator fadeOut;
    private final Mode mode;
    private boolean targetVisible = false;
    private Boolean activeDirectionVisible = null;

    public FadeAnimator(long fadeInMillis, long fadeOutMillis) {
        this(fadeInMillis, fadeOutMillis, 0L, Mode.BOTH);
    }

    public FadeAnimator(long fadeInMillis, long fadeOutMillis, long fadeOutHoldMillis) {
        this(fadeInMillis, fadeOutMillis, fadeOutHoldMillis, Mode.BOTH);
    }

    public FadeAnimator(long fadeInMillis, long fadeOutMillis, long fadeOutHoldMillis, Mode mode) {
        this.fadeIn = new ValueAnimator(fadeInMillis, 0L, ValueAnimator.Easing.LINEAR);
        this.fadeOut = new ValueAnimator(fadeOutMillis, fadeOutHoldMillis, ValueAnimator.Easing.LINEAR);
        this.mode = mode;
    }

    public void setVisible(boolean visible) {
        this.targetVisible = visible;
    }

    public float tick() {
        if (mode == Mode.FADE_OUT && targetVisible) {
            fadeIn.snapTo(1.0f);
            fadeOut.snapTo(1.0f);
            activeDirectionVisible = true;
            return 1.0f;
        }
        if (mode == Mode.FADE_IN && !targetVisible) {
            fadeOut.snapTo(0.0f);
            fadeIn.snapTo(0.0f);
            activeDirectionVisible = false;
            return 0.0f;
        }

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

    public String debugState() {
        return String.format("mode=%s targetVisible=%b activeDirectionVisible=%s fadeIn=%.3f fadeOut=%.3f",
                mode, targetVisible, activeDirectionVisible, fadeIn.getCurrent(), fadeOut.getCurrent());
    }
}