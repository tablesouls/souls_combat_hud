package net.tablesouls.souls_combat_hud.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TexturedToggleButton extends AbstractButton {

    private static final int ROW_NORMAL = 0;
    private static final int ROW_HOVERED = 1;
    private static final int ROW_DISABLED = 2;

    private final ResourceLocation texture;
    private final int texWidth, texHeight;
    private final int texU;
    private final int texV;
    private final OnPress onPress;

    public TexturedToggleButton(int x, int y, int width, int height,
                                Component message, ResourceLocation texture,
                                int texWidth, int texHeight, int texU, int texV,
                                OnPress onPress) {
        super(x, y, width, height, message);
        this.texture = texture;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.texU = texU;
        this.texV = texV;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int row = !this.active ? ROW_DISABLED : (this.isHovered ? ROW_HOVERED : ROW_NORMAL);
        int v = this.texV + row * this.height;

        graphics.blit(this.texture, getX(), getY(), this.texU, v,
                this.width, this.height, this.texWidth, this.texHeight);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public interface OnPress {
        void onPress(TexturedToggleButton button);
    }
}