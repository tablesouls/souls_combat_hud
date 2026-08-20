package net.tablesouls.souls_combat_hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.client.util.TextHelper;
import net.tablesouls.souls_combat_hud.client.util.animation.ExperienceGainAnimator;

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

    private static final int GAIN_TEXT_COLOR = 0xFFFFFF;
    private static final float GAIN_SLIDE_DISTANCE_PX = 10.0f;

    private final ExperienceGainAnimator gainAnimator = new ExperienceGainAnimator();

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

        boolean gainPopupEnabled = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpGainPopup.enabled.get();
        gainAnimator.update(xpTotal, gainPopupEnabled);
        int displayedXpTotal = gainAnimator.getDisplayedTotal();

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
        int barX =  BAR_PADDING_L;
        int barY = BG_HEIGHT - 1;

        int filledWidth = (int)(barWidth * xpProgress);

        //XP icon
        ElementAnchor xpIconAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.anchor.get();

        int iconX = xpIconAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.x.get(),
                ICON_TEX_WIDTH
        );
        int iconY = xpIconAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpIcon.y.get(),
                ICON_TEX_HEIGHT
        );

        //XP total text
        Component xpTotalText = Component.literal(String.valueOf(displayedXpTotal));

        ElementAnchor xpTotalTextAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.anchor.get();
        int xpTotalTextX = xpTotalTextAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.x.get(),
                font.width(xpTotalText)
        );

        int xpTotalTextY = xpTotalTextAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpTotalText.y.get(),
                font.lineHeight - 1
        );

        //XP level text
        Component xpLevelText = Component.literal(String.valueOf(xpLevel));

        ElementAnchor xpLevelTextAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.anchor.get();
        int xpLevelTextX = xpLevelTextAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.x.get(),
                font.width(xpLevelText)
        );

        int xpLevelTextY = xpLevelTextAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpLevelText.y.get(),
                font.lineHeight
        );

        float scale = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.scale.get().floatValue();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(overlayX, overlayY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        guiGraphics.blit(
                BG_TEX,
                0,
                0,
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

        if (gainPopupEnabled && gainAnimator.isPopupVisible()) {
            renderGainPopup(guiGraphics, font);
        }

        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    private void renderGainPopup(GuiGraphics guiGraphics, Font font) {
        Component gainText = Component.literal("+" + gainAnimator.getPendingGain());
        int gainTextWidth = font.width(gainText);

        ElementAnchor gainAnchor = SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpGainPopup.anchor.get();
        int gainX = gainAnchor.resolveX(
                BG_WIDTH,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpGainPopup.x.get(),
                gainTextWidth
        );
        int gainY = gainAnchor.resolveY(
                BG_HEIGHT,
                SoulsCombatHUDConfig.EXPERIENCE_OVERLAY.xpGainPopup.y.get(),
                font.lineHeight
        );

        float popupAlpha = Mth.clamp(gainAnimator.getPopupAlpha(), 0f, 1f);
        if (popupAlpha <= 0f) return;

        float popupScale = gainAnimator.getPopupScale();
        float popupOffsetY = gainAnimator.getPopupOffsetY(GAIN_SLIDE_DISTANCE_PX);

        int alphaBits = Math.round(popupAlpha * 255f) << 24;
        int gainColor = alphaBits | (GAIN_TEXT_COLOR & 0xFFFFFF);

        float pivotX = gainX + gainTextWidth / 2.0f;
        float pivotY = gainY + font.lineHeight / 2.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(pivotX, pivotY + popupOffsetY, 0);
        guiGraphics.pose().scale(popupScale, popupScale, 1.0f);
        guiGraphics.pose().translate(-pivotX, -pivotY, 0);

        guiGraphics.drawString(
                font,
                gainText,
                gainX,
                gainY,
                gainColor,
                false
        );

        guiGraphics.pose().popPose();
    }
}