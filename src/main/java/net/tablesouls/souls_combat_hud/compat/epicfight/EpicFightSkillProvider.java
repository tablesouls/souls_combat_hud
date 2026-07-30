package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.tablesouls.souls_combat_hud.client.render.SkillOverlayRenderer;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;

import java.util.ArrayList;

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
            if (container != null && !container.isEmpty() && SkillOverlayRenderer.shouldDraw(container)) {
                otherSkills.add(container);
            }
        }

        if (!drawWeaponInnate && otherSkills.isEmpty()) {
            return;
        }

        ElementAnchor anchor = SoulsCombatHUDConfig.SKILL_OVERLAY.anchor.get();
        int offsetX = SoulsCombatHUDConfig.SKILL_OVERLAY.x.get();
        int offsetY = SoulsCombatHUDConfig.SKILL_OVERLAY.y.get();

        int rowWidth = SkillOverlayRenderer.rowWidth();
        int rowHeight = SkillOverlayRenderer.rowHeight();
        int rowGap = SkillOverlayRenderer.rowGap();

        int anchorX = anchor.resolveX(screenWidth, offsetX, rowWidth);
        int cursorY = anchor.resolveY(screenHeight, offsetY, rowHeight);
        int step = anchor.isBottom() ? -(rowHeight + rowGap) : (rowHeight + rowGap);

        if (drawWeaponInnate) {
            SkillOverlayRenderer.draw(guiGraphics, font, weaponInnate, anchorX, cursorY, partialTick, anchor.isRight());
            cursorY += step;
        }

        for (SkillContainer container : otherSkills) {
            SkillOverlayRenderer.draw(guiGraphics, font, container, anchorX, cursorY, partialTick, anchor.isRight());
            cursorY += step;
        }
    }
}