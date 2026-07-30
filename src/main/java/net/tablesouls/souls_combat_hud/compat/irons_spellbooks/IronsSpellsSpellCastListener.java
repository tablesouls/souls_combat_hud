package net.tablesouls.souls_combat_hud.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.PartyServerEvents;

public final class IronsSpellsSpellCastListener {
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(IronsSpellsSpellCastListener::onSpellCast);
    }

    private static void onSpellCast(SpellOnCastEvent event) {
        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnSpellCast.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PartyServerEvents.pushResourceMax(player);
    }
}