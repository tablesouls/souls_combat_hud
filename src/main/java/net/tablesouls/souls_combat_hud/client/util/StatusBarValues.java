package net.tablesouls.souls_combat_hud.client.util;

import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

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

    public static double healthWidthCurve() {
        return widthCurve(SoulsCombatHUDConfig.STATS_DATA.health, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.health);
    }

    public static int staminaBaseline(StaminaSourceMode mode) {
        return presetBaseline(
                SoulsCombatHUDConfig.STATS_DATA.stamina, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina,
                mode
        );
    }

    public static int staminaProjectedMax(StaminaSourceMode mode) {
        return presetProjectedMax(
                SoulsCombatHUDConfig.STATS_DATA.stamina, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina,
                mode
        );
    }

    public static double staminaWidthCurve(StaminaSourceMode mode) {
        return presetWidthCurve(
                SoulsCombatHUDConfig.STATS_DATA.stamina, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina,
                mode
        );
    }

    public static int manaBaseline(ManaSourceMode mode) {
        return presetBaseline(
                SoulsCombatHUDConfig.STATS_DATA.mana, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana,
                mode
        );
    }

    public static int manaProjectedMax(ManaSourceMode mode) {
        return presetProjectedMax(
                SoulsCombatHUDConfig.STATS_DATA.mana, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana,
                mode
        );
    }

    public static double manaWidthCurve(ManaSourceMode mode) {
        return presetWidthCurve(
                SoulsCombatHUDConfig.STATS_DATA.mana, SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana,
                mode
        );
    }

    private static int baseline(SoulsCombatHUDConfig.StatThreshold server, SoulsCombatHUDConfig.StatThreshold client) {
        return trustServer() ? server.baseline.get() : client.baseline.get();
    }

    private static int projectedMax(SoulsCombatHUDConfig.StatThreshold server, SoulsCombatHUDConfig.StatThreshold client) {
        int baseline = trustServer() ? server.baseline.get() : client.baseline.get();
        int projectedMax = trustServer() ? server.projectedMax.get() : client.projectedMax.get();
        return resolveProjectedMax(baseline, projectedMax);
    }

    private static double widthCurve(SoulsCombatHUDConfig.StatThreshold server, SoulsCombatHUDConfig.StatThreshold client) {
        return trustServer() ? server.barWidthCurve.get() : client.barWidthCurve.get();
    }

    private static <M extends Enum<M>> int presetBaseline(
            SoulsCombatHUDConfig.PresetStatThreshold<M> server, SoulsCombatHUDConfig.PresetStatThreshold<M> client, M activeMode
    ) {
        SoulsCombatHUDConfig.PresetStatThreshold<M> data = trustServer() ? server : client;
        return data.presets.get(activeMode).baseline.get();
    }

    private static <M extends Enum<M>> int presetProjectedMax(
            SoulsCombatHUDConfig.PresetStatThreshold<M> server, SoulsCombatHUDConfig.PresetStatThreshold<M> client, M activeMode
    ) {
        SoulsCombatHUDConfig.PresetStatThreshold<M> data = trustServer() ? server : client;
        SoulsCombatHUDConfig.StatThreshold preset = data.presets.get(activeMode);
        return resolveProjectedMax(preset.baseline.get(), preset.projectedMax.get());
    }

    private static <M extends Enum<M>> double presetWidthCurve(
            SoulsCombatHUDConfig.PresetStatThreshold<M> server, SoulsCombatHUDConfig.PresetStatThreshold<M> client, M activeMode
    ) {
        SoulsCombatHUDConfig.PresetStatThreshold<M> data = trustServer() ? server : client;
        return data.presets.get(activeMode).barWidthCurve.get();
    }

    public static boolean hasStaminaPreset(StaminaSourceMode mode) {
        if (mode == null) return false;
        SoulsCombatHUDConfig.PresetStatThreshold<StaminaSourceMode> data =
                trustServer() ? SoulsCombatHUDConfig.STATS_DATA.stamina : SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.stamina;
        return data.presets.containsKey(mode);
    }

    public static boolean hasManaPreset(ManaSourceMode mode) {
        if (mode == null) return false;
        SoulsCombatHUDConfig.PresetStatThreshold<ManaSourceMode> data =
                trustServer() ? SoulsCombatHUDConfig.STATS_DATA.mana : SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.mana;
        return data.presets.containsKey(mode);
    }

    private static int resolveProjectedMax(int baseline, int projectedMax) {
        return Math.max(baseline + 1, projectedMax);
    }
}