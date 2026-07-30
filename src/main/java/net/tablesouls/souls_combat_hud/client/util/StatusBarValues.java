package net.tablesouls.souls_combat_hud.client.util;

import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

public final class StatusBarValues {
    private static boolean trustServer() {
        return SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.trustServerValues.get();
    }

    public static int healthBaseline() {
        return baseline(SoulsCombatHUDConfig.STATS_DATA.health, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.health);
    }

    public static int healthProjectedMax() {
        return projectedMax(SoulsCombatHUDConfig.STATS_DATA.health, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.health);
    }

    public static int staminaBaseline() {
        return baseline(SoulsCombatHUDConfig.STATS_DATA.stamina, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina);
    }

    public static int staminaProjectedMax() {
        return projectedMax(SoulsCombatHUDConfig.STATS_DATA.stamina, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina);
    }

    public static int manaBaseline() {
        return baseline(SoulsCombatHUDConfig.STATS_DATA.mana, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana);
    }

    public static int manaProjectedMax() {
        return projectedMax(SoulsCombatHUDConfig.STATS_DATA.mana, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana);
    }

    private static int baseline(SoulsCombatHUDConfig.StatThreshold server, SoulsCombatHUDConfig.StatThreshold client) {
        return trustServer() ? server.baseline.get() : client.baseline.get();
    }

    private static int projectedMax(SoulsCombatHUDConfig.StatThreshold server, SoulsCombatHUDConfig.StatThreshold client) {
        int baseline = trustServer() ? server.baseline.get() : client.baseline.get();
        int projectedMax = trustServer() ? server.projectedMax.get() : client.projectedMax.get();
        return resolveProjectedMax(baseline, projectedMax);
    }

    private static int resolveProjectedMax(int baseline, int projectedMax) {
        return Math.max(baseline + 1, projectedMax);
    }
}