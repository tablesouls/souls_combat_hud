package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.ForgeConfigSpec;
import net.tablesouls.souls_combat_hud.client.render.SkillOverlayRenderer;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.debug.DebugLogger;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

import java.util.ArrayList;
import java.util.List;

public final class EpicFightSkillProvider {
    public static void renderSkillOverlay(
            ForgeGui gui,
            GuiGraphics guiGraphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        LocalPlayerPatch playerpatch = (LocalPlayerPatch) ClientEngine.getInstance().getPlayerPatch();
        if (playerpatch == null) {
            return;
        }

        if (SoulsCombatHUDConfig.DEBUG_CLIENT.enabled.get()) {
            DebugLogger.logSkillIds(playerpatch);
        }

        Font font = minecraft.font;

        SkillContainer weaponInnate = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
        boolean drawWeaponInnate = weaponInnate != null
                && !weaponInnate.isEmpty()
                && SkillOverlayRenderer.shouldDraw(weaponInnate);

        ArrayList<SkillContainer> otherSkills = new ArrayList<>();
        for (SkillSlot slot : SkillSlot.ENUM_MANAGER.universalValues()) {
            if (slot == SkillSlots.WEAPON_INNATE) {
                continue;
            }

            SkillContainer container = playerpatch.getSkill(slot);
            if (container == null || container.isEmpty() || !SkillOverlayRenderer.shouldDraw(container)) {
                continue;
            }

            if (!isSlotHandledByOverlay(container)) {
                continue;
            }

            otherSkills.add(container);
        }

        if (!drawWeaponInnate && otherSkills.isEmpty()) {
            return;
        }

        ElementAnchor anchor = SoulsCombatHUDConfig.SKILL_OVERLAY.anchor.get();
        int offsetX = SoulsCombatHUDConfig.SKILL_OVERLAY.x.get();
        int offsetY = SoulsCombatHUDConfig.SKILL_OVERLAY.y.get();
        float scale = SoulsCombatHUDConfig.SKILL_OVERLAY.scale.get().floatValue();

        int rowWidth = SkillOverlayRenderer.rowWidth();
        int rowHeight = SkillOverlayRenderer.rowHeight();
        int rowGap = SkillOverlayRenderer.rowGap();

        int anchorX = anchor.resolveX(screenWidth, offsetX, rowWidth);
        int anchorY = anchor.resolveY(screenHeight, offsetY, rowHeight);
        int step = anchor.isBottom() ? -(rowHeight + rowGap) : (rowHeight + rowGap);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(anchorX, anchorY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        int cursorY = 0;

        if (drawWeaponInnate) {
            SkillOverlayRenderer.draw(guiGraphics, font, weaponInnate, 0, cursorY, partialTick, anchor.isRight());
            cursorY += step;
        }

        for (SkillContainer container : otherSkills) {
            SkillOverlayRenderer.draw(guiGraphics, font, container, 0, cursorY, partialTick, anchor.isRight());
            cursorY += step;
        }

        guiGraphics.pose().popPose();
    }

    private static boolean isPassiveSlot(SkillSlot slot) {
        return slot == SkillSlots.PASSIVE1 || slot == SkillSlots.PASSIVE2 || slot == SkillSlots.PASSIVE3;
    }

    private static boolean isCategoryHandledByOverlay(
            SkillContainer container,
            ForgeConfigSpec.BooleanValue categoryEnabled,
            ForgeConfigSpec.ConfigValue<List<? extends String>> blacklist
    ) {
        if (!categoryEnabled.get()) {
            return false;
        }
        ResourceLocation skillId = container.getSkill().getRegistryName();
        return skillId == null || !blacklist.get().contains(skillId.toString());
    }

    public static boolean isSlotHandledByOverlay(SkillContainer container) {
        if (!SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get()) {
            return false;
        }

        SkillSlot slot = container.getSlot();
        var settings = SoulsCombatHUDConfig.SKILL_OVERLAY.skillSetting;

        if (slot == SkillSlots.WEAPON_PASSIVE) {
            return isCategoryHandledByOverlay(container, settings.weaponPassiveSkill, settings.weaponPassiveSkillBlacklist);
        }
        if (isPassiveSlot(slot)) {
            return isCategoryHandledByOverlay(container, settings.passiveSkills, settings.passiveSkillsBlacklist);
        }
        if (slot == SkillSlots.GUARD) {
            return isCategoryHandledByOverlay(container, settings.guardSkill, settings.guardSkillBlacklist);
        }
        if (slot == SkillSlots.IDENTITY) {
            return isCategoryHandledByOverlay(container, settings.identitySkill, settings.identitySkillBlacklist);
        }
        return false;
    }
}