package net.tablesouls.souls_combat_hud.util;

public enum ElementAnchor {
    TOP_LEFT(Horizontal.LEFT, Vertical.TOP),
    TOP_CENTER(Horizontal.CENTER, Vertical.TOP),
    TOP_RIGHT(Horizontal.RIGHT, Vertical.TOP),
    CENTER_LEFT(Horizontal.LEFT, Vertical.CENTER),
    CENTER(Horizontal.CENTER, Vertical.CENTER),
    CENTER_RIGHT(Horizontal.RIGHT, Vertical.CENTER),
    BOTTOM_LEFT(Horizontal.LEFT, Vertical.BOTTOM),
    BOTTOM_CENTER(Horizontal.CENTER, Vertical.BOTTOM),
    BOTTOM_RIGHT(Horizontal.RIGHT, Vertical.BOTTOM);

    public enum Horizontal { LEFT, CENTER, RIGHT }
    public enum Vertical { TOP, CENTER, BOTTOM }

    private final Horizontal horizontal;
    private final Vertical vertical;

    ElementAnchor(Horizontal horizontal, Vertical vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Horizontal horizontal() { return this.horizontal; }
    public Vertical vertical() { return this.vertical; }

    public boolean isRight() { return this.horizontal == Horizontal.RIGHT; }
    public boolean isBottom() { return this.vertical == Vertical.BOTTOM; }

    public int resolveX(int screenWidth, int offsetX, int width) {
        switch (this.horizontal) {
            case RIGHT:  return screenWidth - offsetX - width;
            case CENTER: return (screenWidth - width) / 2 - offsetX;
            default:     return offsetX;
        }
    }

    public int resolveY(int screenHeight, int offsetY, int height) {
        switch (this.vertical) {
            case BOTTOM: return screenHeight - offsetY - height;
            case CENTER: return (screenHeight - height) / 2 - offsetY;
            default:     return offsetY;
        }
    }
}