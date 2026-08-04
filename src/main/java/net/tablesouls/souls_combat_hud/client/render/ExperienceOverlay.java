package net.tablesouls.souls_combat_hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.client.util.TextHelper;

public class ExperienceOverlay implements IGuiOverlay {
    private static final ResourceLocation BG_TEX =
            ResourceLocation.fromNamespaceAndPath(
                    SoulsCombatHUD.MODID,
                    "textures/gui/experience_background.png");
    private static final ResourceLocation ICON_TEX =
            ResourceLocation.fromNamespaceAndPath(
                    SoulsCombatHUD.MODID,
                    "textures/gui/xp_icon.png");

    private static final int BG_TEX_WIDTH = 128;
    private static final int BG_TEX_HEIGHT = 32;
    private static final int BG_WIDTH = 80;
    private static final int BG_HEIGHT = 14;
    private static final int BG_U = 0;
    private static final int BG_V = 0;

    private static final int ICON_TEX_WIDTH = 16;
    private static final int ICON_TEX_HEIGHT = 16;
    private static final int ICON_U = 0;
    private static final int ICON_V = 0;

    private static final int BAR_HEIGHT = 1;
    private static final int BAR_PADDING_L = 1;
    private static final int BAR_PADDING_R = 1;

    @Override
    public void render(
            ForgeGui gui,
            GuiGraphics guiGraphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        if (!SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.enabled.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        Font font = minecraft.font;
        int xpLevel = minecraft.player.experienceLevel;
        int xpTotal = minecraft.player.totalExperience;
        float xpProgress = minecraft.player.experienceProgress;

        ElementAnchor anchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.anchor.get();
        int overlayX = anchor.resolveX(
                screenWidth,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.x.get(),
                BG_WIDTH);
        int overlayY = anchor.resolveY(screenHeight,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.y.get(),
                BG_HEIGHT);

        //XP bar
        int barWidth = BG_WIDTH - BAR_PADDING_L - BAR_PADDING_R;
        int barX = overlayX + BAR_PADDING_L;
        int barY = overlayY + BG_HEIGHT - 1;

        int filledWidth = (int)(barWidth * xpProgress);

        //XP icon
        ElementAnchor xpIconAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.anchor.get();

        int iconX = overlayX + xpIconAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.x.get(),
                ICON_TEX_WIDTH
        );
        int iconY = overlayY + xpIconAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.y.get(),
                ICON_TEX_HEIGHT
        );

        //XP total xp text
        Component xpTotalText = Component.literal(String.valueOf(xpTotal));

        ElementAnchor xpTotalTextAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.anchor.get();
        int xpTotalTextX = overlayX + xpTotalTextAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.x.get(),
                font.width(xpTotalText)
        );

        int xpTotalTextY = overlayY + xpTotalTextAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.y.get(),
                font.lineHeight - 1
        );

        //XP level text
        Component xpLevelText = Component.literal(String.valueOf(xpLevel));

        ElementAnchor xpLevelTextAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.anchor.get();
        int xpLevelTextX = overlayX + xpLevelTextAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.x.get(),
                font.width(xpLevelText)
        );

        int xpLevelTextY = overlayY + xpLevelTextAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.y.get(),
                font.lineHeight
        );
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.blit(
                BG_TEX,
                overlayX,
                overlayY,
                BG_U,
                BG_V,
                BG_WIDTH,
                BG_HEIGHT,
                BG_TEX_WIDTH,
                BG_TEX_HEIGHT
        );

        if (SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpBar.enabled.get()) {
            guiGraphics.fill(
                    barX,
                    barY,
                    barX + filledWidth,
                    barY +  BAR_HEIGHT,
                    0xFF00FF00
            );
        }

        if (SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.enabled.get()) {
            guiGraphics.blit(
                    ICON_TEX,
                    iconX,
                    iconY,
                    ICON_U,
                    ICON_V,
                    ICON_TEX_WIDTH,
                    ICON_TEX_HEIGHT,
                    ICON_TEX_WIDTH,
                    ICON_TEX_HEIGHT
            );
        }

        if (SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.enabled.get()) {
            guiGraphics.drawString(
                    font,
                    xpTotalText,
                    xpTotalTextX,
                    xpTotalTextY,
                    0xFFFFFF,
                    false
            );
        }

        if (SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.enabled.get()) {
            TextHelper.drawOutlinedString(
                    guiGraphics,
                    font,
                    xpLevelText,
                    xpLevelTextX,
                    xpLevelTextY,
                    0xc8ff8f,
                    0x2d2102
            );
        }

        RenderSystem.disableBlend();
    }
}
