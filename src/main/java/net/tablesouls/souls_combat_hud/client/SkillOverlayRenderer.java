package net.tablesouls.souls_combat_hud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

import java.util.HashMap;
import java.util.Map;

public final class SkillOverlayRenderer {
    private static final ResourceLocation BG_TEX =
            ResourceLocation.fromNamespaceAndPath("souls_combat_hud", "textures/gui/skill_row_background.png");

    private static final int BG_TEX_W = 128;
    private static final int BG_TEX_H = 32;
    private static final int ROW_U = 0;
    private static final int ROW_V = 0;
    private static final int ROW_WIDTH = 112;
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_GAP = 2;

    private static final int ICON_ZONE = 14;
    private static final int TARGET_ICON_SIZE = 16;
    private static final int TEXT_PADDING_LEFT = 8;
    private static final int TEXT_PADDING_RIGHT = 8;
    private static final int TEXT_ZONE_WIDTH = ROW_WIDTH - ICON_ZONE - TEXT_PADDING_LEFT - TEXT_PADDING_RIGHT;

    private static final int TEXT_COLOR_READY = 0xFFFFFF;
    private static final int TEXT_COLOR_DIM = 0x808080;

    public static boolean shouldDraw(SkillContainer container) {
        return !container.isEmpty() && container.getSkill().shouldDraw(container);
    }

    public static int draw(
            GuiGraphics guiGraphics,
            Font font, SkillContainer container,
            int topLeftX,
            int topLeftY,
            float partialTick,
            boolean mirrored
    ) {
        Skill skill = container.getSkill();
        boolean canUse = !container.isDisabled() && skill.checkExecuteCondition(container);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(BG_TEX, topLeftX, topLeftY, ROW_U, ROW_V, ROW_WIDTH, ROW_HEIGHT, BG_TEX_W, BG_TEX_H);

        int iconX = mirrored
                ? topLeftX + ROW_WIDTH - ICON_ZONE + (ICON_ZONE - TARGET_ICON_SIZE) /2 - 4
                : topLeftX + (ICON_ZONE - TARGET_ICON_SIZE) /2 + 4;

        int iconY = topLeftY + (ROW_HEIGHT - TARGET_ICON_SIZE) / 2;
        drawSkillIcon(guiGraphics, container, iconX, iconY, partialTick);

        String name = resolveSkillName(skill);
        int textWidth = font.width(name);
        if (textWidth > TEXT_ZONE_WIDTH) {
            name = font.plainSubstrByWidth(name, TEXT_ZONE_WIDTH);
            textWidth = font.width(name);
        }

        int color = canUse ? TEXT_COLOR_READY : TEXT_COLOR_DIM;
        int textX = mirrored
                ? topLeftX + ROW_WIDTH - ICON_ZONE - TEXT_PADDING_RIGHT - textWidth
                : topLeftX + ICON_ZONE + TEXT_PADDING_LEFT;

        int textY = topLeftY + (ROW_HEIGHT - font.lineHeight) / 2 + 1;

        guiGraphics.drawString(font, name, textX, textY, color, false);

        return topLeftY + ROW_HEIGHT;
    }

    private static void drawSkillIcon(
            GuiGraphics guiGraphics,
            SkillContainer container,
            int iconX,
            int iconY,
            float partialTick
    ) {
        RenderEngine renderEngine = ClientEngine.getInstance().renderEngine;
        BattleModeGui battleModeGui = renderEngine.battleModeUI;

        float nativeIconSize = (container.getSkill() instanceof WeaponInnateSkill) ? 32.0f : 24.0f;
        float scale = 16.0f / nativeIconSize;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        try {
            poseStack.translate(iconX, iconY, 0.0f);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(0.0f, -battleModeGui.getSlidingProgression(), 0.0f);
            container.getSkill().drawOnGui(battleModeGui, container, guiGraphics, 0.0f, 0.0f, partialTick);
        } finally {
            poseStack.popPose();
        }
    }

    private static final Map<ResourceLocation, String> SKILL_NAME_CACHE = new HashMap<>();

    private static String resolveSkillName(Skill skill) {
        ResourceLocation registryName = skill.getRegistryName();
        if (registryName == null) {
            return skill.toString();
        }
        return SKILL_NAME_CACHE.computeIfAbsent(registryName, id -> {
            String translationKey = String.format("skill.%s.%s", registryName.getNamespace(), registryName.getPath());
            return Component.translatable(translationKey).getString();
        });
    }

    public static int rowWidth() {
        return ROW_WIDTH;
    }

    public static int rowHeight() {
        return ROW_HEIGHT;
    }

    public static int rowGap() {
        return ROW_GAP;
    }
}