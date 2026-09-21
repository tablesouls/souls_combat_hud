package net.tablesouls.souls_combat_hud.debug;

import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;

import java.util.ArrayList;
import java.util.List;

public class DebugLogger {
    private static String lastLoggedSkillIds = null;

    private static String skillIdOf(SkillContainer container) {
        ResourceLocation id = container.getSkill().getRegistryName();
        return id == null ? container.getSkill().toString() : id.toString();
    }

    public static void logSkillIds(LocalPlayerPatch playerpatch) {
        List<String> entries = new ArrayList<>();

        for (SkillSlot slot : SkillSlot.ENUM_MANAGER.universalValues()) {
            SkillContainer container = playerpatch.getSkill(slot);
            if (container == null || container.isEmpty()) continue;

            entries.add(slot + "=" + skillIdOf(container));
        }

        String signature = String.join(", ", entries);
        if (signature.equals(lastLoggedSkillIds)) return;
        lastLoggedSkillIds = signature;

        if (entries.isEmpty()) return;

        SoulsCombatHUD.LOGGER.info("[SkillOverlay] {}", signature);
    }
}