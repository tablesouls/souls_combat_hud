package net.tablesouls.souls_combat_hud.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BooleanSupplier;

public class TexturedStateButton extends AbstractButton {

    private final ResourceLocation texture;
    private final int texSheetWidth, texSheetHeight;
    private final int texU, texV;
    private final int frameWidth, frameHeight;
    private final BooleanSupplier state;
    private final OnPress onPress;

    public TexturedStateButton(int x, int y, int width, int height,
                               Component message,
                               ResourceLocation texture, int texSheetWidth, int texSheetHeight,
                               int texU, int texV,
                               BooleanSupplier state, OnPress onPress) {
        super(x, y, width, height, message);
        this.frameWidth = width;
        this.frameHeight = height;
        this.texture = texture;
        this.texSheetWidth = texSheetWidth;
        this.texSheetHeight = texSheetHeight;
        this.texU = texU;
        this.texV = texV;
        this.state = state;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int col = this.state.getAsBoolean() ? 1 : 0;
        int row = this.isHovered ? 1 : 0;

        int u = this.texU + col * this.frameWidth;
        int v = this.texV + row * this.frameHeight;

        graphics.blit(this.texture, getX(), getY(), u, v,
                this.frameWidth, this.frameHeight, this.texSheetWidth, this.texSheetHeight);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public void refreshTooltip(Component message) {
        this.setMessage(message);
        this.setTooltip(Tooltip.create(message));
    }

    public interface OnPress {
        void onPress(TexturedStateButton button);
    }
}