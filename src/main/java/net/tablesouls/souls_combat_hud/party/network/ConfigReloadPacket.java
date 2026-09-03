package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigReloadPacket {
    private final int healthBaseline;
    private final int healthProjectedMax;
    private final double healthWidthCurve;

    private final Map<StaminaSourceMode, ThresholdSnapshot> staminaPresets;
    private final Map<ManaSourceMode, ThresholdSnapshot> manaPresets;

    public ConfigReloadPacket(
            int healthBaseline, int healthProjectedMax, double healthWidthCurve,
            Map<StaminaSourceMode, ThresholdSnapshot> staminaPresets,
            Map<ManaSourceMode, ThresholdSnapshot> manaPresets
    ) {
        this.healthBaseline = healthBaseline;
        this.healthProjectedMax = healthProjectedMax;
        this.healthWidthCurve = healthWidthCurve;
        this.staminaPresets = staminaPresets;
        this.manaPresets = manaPresets;
    }

    private record ThresholdSnapshot(int baseline, int projectedMax, double barWidthCurve) {}

    public static ConfigReloadPacket fromCurrentServerValues() {
        SoulsCombatHUDConfig.StatsData stats = SoulsCombatHUDConfig.STATS_DATA;
        return new ConfigReloadPacket(
                stats.health.baseline.get(), stats.health.projectedMax.get(), stats.health.barWidthCurve.get(),
                snapshotPresets(stats.stamina.presets, StaminaSourceMode.class),
                snapshotPresets(stats.mana.presets, ManaSourceMode.class)
        );
    }

    private static <M extends Enum<M>> Map<M, ThresholdSnapshot> snapshotPresets(
            Map<M, SoulsCombatHUDConfig.StatThreshold> presets, Class<M> modeClass) {
        Map<M, ThresholdSnapshot> snapshot = new EnumMap<>(modeClass);
        for (Map.Entry<M, SoulsCombatHUDConfig.StatThreshold> entry : presets.entrySet()) {
            SoulsCombatHUDConfig.StatThreshold threshold = entry.getValue();
            snapshot.put(entry.getKey(), new ThresholdSnapshot(
                    threshold.baseline.get(), threshold.projectedMax.get(), threshold.barWidthCurve.get()
            ));
        }
        return snapshot;
    }

    public static void encode(ConfigReloadPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.healthBaseline);
        buf.writeVarInt(packet.healthProjectedMax);
        buf.writeDouble(packet.healthWidthCurve);

        writePresets(buf, packet.staminaPresets);
        writePresets(buf, packet.manaPresets);
    }

    private static <M extends Enum<M>> void writePresets(FriendlyByteBuf buf, Map<M, ThresholdSnapshot> presets) {
        buf.writeVarInt(presets.size());
        for (Map.Entry<M, ThresholdSnapshot> entry : presets.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeVarInt(entry.getValue().baseline());
            buf.writeVarInt(entry.getValue().projectedMax());
            buf.writeDouble(entry.getValue().barWidthCurve());
        }
    }

    private static <M extends Enum<M>> Map<M, ThresholdSnapshot> readPresets(FriendlyByteBuf buf, Class<M> modeClass) {
        int size = buf.readVarInt();
        Map<M, ThresholdSnapshot> presets = new EnumMap<>(modeClass);
        for (int i = 0; i < size; i++) {
            M mode = buf.readEnum(modeClass);
            int baseline = buf.readVarInt();
            int projectedMax = buf.readVarInt();
            double barWidthCurve = buf.readDouble();
            presets.put(mode, new ThresholdSnapshot(baseline, projectedMax, barWidthCurve));
        }
        return presets;
    }

    public static ConfigReloadPacket decode(FriendlyByteBuf buf) {
        int healthBaseline = buf.readVarInt();
        int healthProjectedMax = buf.readVarInt();
        double healthWidthCurve = buf.readDouble();

        Map<StaminaSourceMode, ThresholdSnapshot> staminaPresets = readPresets(buf, StaminaSourceMode.class);
        Map<ManaSourceMode, ThresholdSnapshot> manaPresets = readPresets(buf, ManaSourceMode.class);

        return new ConfigReloadPacket(
                healthBaseline, healthProjectedMax, healthWidthCurve,
                staminaPresets, manaPresets
        );
    }

    public static void handle(ConfigReloadPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            SoulsCombatHUDConfig.StatsData stats = SoulsCombatHUDConfig.STATS_DATA;

            stats.health.baseline.set(packet.healthBaseline);
            stats.health.projectedMax.set(packet.healthProjectedMax);
            stats.health.barWidthCurve.set(packet.healthWidthCurve);

            applyPresets(stats.stamina.presets, packet.staminaPresets);
            applyPresets(stats.mana.presets, packet.manaPresets);
        });
        ctx.setPacketHandled(true);
    }

    private static <M extends Enum<M>> void applyPresets(
            Map<M, SoulsCombatHUDConfig.StatThreshold> presets, Map<M, ThresholdSnapshot> incoming) {
        for (Map.Entry<M, ThresholdSnapshot> entry : incoming.entrySet()) {
            SoulsCombatHUDConfig.StatThreshold preset = presets.get(entry.getKey());
            if (preset == null) continue;
            ThresholdSnapshot snapshot = entry.getValue();
            preset.baseline.set(snapshot.baseline());
            preset.projectedMax.set(snapshot.projectedMax());
            preset.barWidthCurve.set(snapshot.barWidthCurve());
        }
    }
}