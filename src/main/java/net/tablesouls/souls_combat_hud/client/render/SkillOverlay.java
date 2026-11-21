package net.tablesouls.souls_combat_hud.client.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.tablesouls.ts_ui_lib.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightSkillProvider;

public class SkillOverlay implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!EpicFightCompat.LOADED) return;

        if (!SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get()) return;

        EpicFightSkillProvider.renderSkillOverlay(
                Minecraft.getInstance().gui,
                guiGraphics,
                deltaTracker.getGameTimeDeltaPartialTick(false),
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight()
        );
    }
}
