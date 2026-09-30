package net.tablesouls.souls_combat_hud.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightSkillProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

@Mixin(value = BattleModeGui.class, remap = false)
public class BattleModeGuiMixin {
    @Redirect(
            method = "renderNormalSkills",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/skill/Skill;drawOnGui(Lyesman/epicfight/client/gui/BattleModeGui;Lyesman/epicfight/skill/SkillContainer;Lnet/minecraft/client/gui/GuiGraphics;FFF)V",
                    remap = false
            ),
            remap = false
    )
    private void souls_combat_hud$drawNativePassiveSkill(
            Skill skill,
            BattleModeGui gui,
            SkillContainer container,
            GuiGraphics guiGraphics,
            float x,
            float y,
            float partialTick
    ) {
        if (EpicFightSkillProvider.isSlotHandledByOverlay(container)) {
            return;
        }
        skill.drawOnGui(gui, container, guiGraphics, x, y, partialTick);
    }
}