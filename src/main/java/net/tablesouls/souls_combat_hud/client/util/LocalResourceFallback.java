package net.tablesouls.souls_combat_hud.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.tablesouls.souls_combat_hud.compat.AbstractResourceSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.SourceMode;
import net.tablesouls.souls_combat_hud.party.network.PartyNetwork;

public final class LocalResourceFallback {
    private static final int GRACE_TICKS = 60;

    private static int ticksSinceLogin = -1;
    private static boolean resolved = false;

    private LocalResourceFallback() {}

    public static void reset() {
        ticksSinceLogin = 0;
        resolved = false;
    }

    public static void tick() {
        if (ticksSinceLogin < 0 || resolved) return;

        if (PartyNetwork.serverSupportsParty()) {
            resolved = true;
            return;
        }

        if (++ticksSinceLogin < GRACE_TICKS) return;

        resolved = true;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        applyLocalGuess(StaminaSourceRegistry.INSTANCE,
                SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.clientStaminaSource.get(), player);
        applyLocalGuess(ManaSourceRegistry.INSTANCE,
                SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.clientManaSource.get(), player);
    }

    private static <M extends Enum<M> & SourceMode> void applyLocalGuess(
            AbstractResourceSourceRegistry<M> registry, M localPreference, LocalPlayer player) {
        AbstractResourceSourceRegistry.Resolution<M> resolution =
                registry.resolveServerSide(player, localPreference);
        if (resolution.mode() != null) {
            registry.setActiveMode(resolution.mode());
        }
    }
}