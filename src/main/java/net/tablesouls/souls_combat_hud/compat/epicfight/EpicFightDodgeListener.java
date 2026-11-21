package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.network.PartyServerEvents;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillConsumeEvent;
import yesman.epicfight.skill.Skill;

public final class EpicFightDodgeListener {
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        EpicFightEventHooks.Player.CONSUME_SKILL.registerEvent(
                EpicFightDodgeListener::onSkillConsume,
                "souls_combat_hud:stamina_consume"
        );
    }

    private static void onSkillConsume(SkillConsumeEvent event) {
        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnDodge.get()) return;
        if (event.getResourceType() != Skill.Resource.STAMINA) return;
        if (!(event.getEntityPatch().getOriginal() instanceof ServerPlayer player)) return;

        PartyServerEvents.pushResourceMax(player);
    }
}