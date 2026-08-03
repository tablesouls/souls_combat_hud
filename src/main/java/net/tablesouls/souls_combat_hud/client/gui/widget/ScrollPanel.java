package net.tablesouls.souls_combat_hud.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ScrollPanel {
    public record ScrollbarStyle(ResourceLocation texture, int width, int thumbHeight,
                                 int texWidth, int texHeight, int u, int v,
                                 int trackColor) {
    }

    private final ScrollbarStyle style;
    private final double scrollPerNotch;

    private int x0, y0, x1, y1;
    private int scrollbarX;

    private int contentHeight = 0;

    private double scrollAmount = 0;
    private boolean draggingScrollbar = false;
    private int scrollbarThumbY;
    private int scrollbarThumbHeight;

    public ScrollPanel(ScrollbarStyle style, double scrollPerNotch) {
        this.style = style;
        this.scrollPerNotch = scrollPerNotch;
    }

    public void setViewport(int x0, int y0, int x1, int y1, int scrollbarX) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.scrollbarX = scrollbarX;
        clampScroll();
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
        clampScroll();
    }

    private void clampScroll() {
        this.scrollAmount = Math.min(this.scrollAmount, maxScroll());
    }

    public double maxScroll() {
        int viewportHeight = this.y1 - this.y0;
        return Math.max(0, this.contentHeight - viewportHeight);
    }

    public int getOffset() {
        return (int) Math.round(this.scrollAmount);
    }

    public int viewportTop() { return this.y0; }
    public int viewportBottom() { return this.y1; }

    public void scrollIntoView(int contentTop, int contentBottom) {
        int viewportHeight = this.y1 - this.y0;
        if (contentTop < this.scrollAmount) {
            this.scrollAmount = Math.max(0, contentTop);
        } else if (contentBottom > this.scrollAmount + viewportHeight) {
            this.scrollAmount = Math.min(maxScroll(), contentBottom - viewportHeight);
        }
    }

    public void renderContent(GuiGraphics graphics, Runnable contentRenderer) {
        graphics.enableScissor(this.x0, this.y0, this.x1, this.y1);
        contentRenderer.run();
        graphics.disableScissor();
    }

    public void renderScrollbar(GuiGraphics graphics) {
        double max = maxScroll();
        if (max <= 0) {
            this.scrollbarThumbHeight = 0;
            return;
        }

        int viewportHeight = this.y1 - this.y0;
        int thumbHeight = Math.min(viewportHeight, this.style.thumbHeight());
        int track = Math.max(1, viewportHeight - thumbHeight);
        int thumbY = this.y0 + (int) Math.round(track * (this.scrollAmount / max));

        this.scrollbarThumbY = thumbY;
        this.scrollbarThumbHeight = thumbHeight;

        graphics.fill(this.scrollbarX, this.y0, this.scrollbarX + this.style.width(), this.y1, this.style.trackColor());
        graphics.blit(this.style.texture(), this.scrollbarX, thumbY, this.style.u(), this.style.v(),
                this.style.width(), thumbHeight, this.style.texWidth(), this.style.texHeight());
    }

    public boolean mouseScrolled(double delta) {
        double max = maxScroll();
        if (max <= 0) {
            return false;
        }
        this.scrollAmount = Math.max(0, Math.min(max, this.scrollAmount - delta * this.scrollPerNotch));
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.scrollbarThumbHeight > 0
                && mouseX >= this.scrollbarX && mouseX < this.scrollbarX + this.style.width()
                && mouseY >= this.y0 && mouseY < this.y1) {
            this.draggingScrollbar = true;
            dragTo(mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseY) {
        if (this.draggingScrollbar) {
            dragTo(mouseY);
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return false;
    }

    private void dragTo(double mouseY) {
        double max = maxScroll();
        if (max <= 0) return;

        int viewportHeight = this.y1 - this.y0;
        int track = Math.max(1, viewportHeight - this.scrollbarThumbHeight);
        double fraction = (mouseY - this.scrollbarThumbHeight / 2.0 - this.y0) / track;

        this.scrollAmount = Math.max(0, Math.min(max, fraction * max));
    }
}