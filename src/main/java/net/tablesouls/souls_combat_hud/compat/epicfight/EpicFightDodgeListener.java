package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.network.PartyServerEvents;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class EpicFightDodgeListener {
    private static final UUID LISTENER_ID =
            UUID.nameUUIDFromBytes("souls_combat_hud:stamina_consume".getBytes(StandardCharsets.UTF_8));

    public static void register(ServerPlayer player) {
        ServerPlayerPatch patch = EpicFightCapabilities.getServerPlayerPatch(player);
        if (patch == null) return;

        patch.getEventListener().addEventListener(
                PlayerEventListener.EventType.SKILL_CONSUME_EVENT,
                LISTENER_ID,
                EpicFightDodgeListener::onSkillConsume
        );
    }

    private static void onSkillConsume(SkillConsumeEvent event) {
        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnDodge.get()) return;
        if (event.getResourceType() != Skill.Resource.STAMINA) return;

        Player original = event.getPlayerPatch().getOriginal();
        if (!(original instanceof ServerPlayer player)) return;

        PartyServerEvents.pushResourceMax(player);
    }
}