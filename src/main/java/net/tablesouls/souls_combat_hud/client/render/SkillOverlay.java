package net.tablesouls.souls_combat_hud.client.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightSkillProvider;

public class SkillOverlay implements IGuiOverlay {

    @Override
    public void render(
            ForgeGui gui,
            GuiGraphics guiGraphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        if (!EpicFightCompat.LOADED) return;

        if (!SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get()) return;

        EpicFightSkillProvider.renderSkillOverlay(gui, guiGraphics, partialTick, screenWidth, screenHeight);
    }
}