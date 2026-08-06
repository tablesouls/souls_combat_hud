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

    private final int staminaBaseline;
    private final int staminaProjectedMax;
    private final Map<StaminaSourceMode, int[]> staminaPresets;

    private final int manaBaseline;
    private final int manaProjectedMax;
    private final Map<ManaSourceMode, int[]> manaPresets;

    public ConfigReloadPacket(
            int healthBaseline, int healthProjectedMax,
            int staminaBaseline, int staminaProjectedMax, Map<StaminaSourceMode, int[]> staminaPresets,
            int manaBaseline, int manaProjectedMax, Map<ManaSourceMode, int[]> manaPresets
    ) {
        this.healthBaseline = healthBaseline;
        this.healthProjectedMax = healthProjectedMax;
        this.staminaBaseline = staminaBaseline;
        this.staminaProjectedMax = staminaProjectedMax;
        this.staminaPresets = staminaPresets;
        this.manaBaseline = manaBaseline;
        this.manaProjectedMax = manaProjectedMax;
        this.manaPresets = manaPresets;
    }

    public static ConfigReloadPacket fromCurrentServerValues() {
        SoulsCombatHUDConfig.StatsData stats = SoulsCombatHUDConfig.STATS_DATA;
        return new ConfigReloadPacket(
                stats.health.baseline.get(), stats.health.projectedMax.get(),
                stats.stamina.baseline.get(), stats.stamina.projectedMax.get(),
                snapshotPresets(stats.stamina.presets, StaminaSourceMode.class),
                stats.mana.baseline.get(), stats.mana.projectedMax.get(),
                snapshotPresets(stats.mana.presets, ManaSourceMode.class)
        );
    }

    private static <M extends Enum<M>> Map<M, int[]> snapshotPresets(
            Map<M, SoulsCombatHUDConfig.StatThreshold> presets, Class<M> modeClass) {
        Map<M, int[]> snapshot = new EnumMap<>(modeClass);
        for (Map.Entry<M, SoulsCombatHUDConfig.StatThreshold> entry : presets.entrySet()) {
            snapshot.put(entry.getKey(), new int[]{entry.getValue().baseline.get(), entry.getValue().projectedMax.get()});
        }
        return snapshot;
    }

    public static void encode(ConfigReloadPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.healthBaseline);
        buf.writeVarInt(packet.healthProjectedMax);

        buf.writeVarInt(packet.staminaBaseline);
        buf.writeVarInt(packet.staminaProjectedMax);
        writePresets(buf, packet.staminaPresets);

        buf.writeVarInt(packet.manaBaseline);
        buf.writeVarInt(packet.manaProjectedMax);
        writePresets(buf, packet.manaPresets);
    }

    private static <M extends Enum<M>> void writePresets(FriendlyByteBuf buf, Map<M, int[]> presets) {
        buf.writeVarInt(presets.size());
        for (Map.Entry<M, int[]> entry : presets.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeVarInt(entry.getValue()[0]);
            buf.writeVarInt(entry.getValue()[1]);
        }
    }

    private static <M extends Enum<M>> Map<M, int[]> readPresets(FriendlyByteBuf buf, Class<M> modeClass) {
        int size = buf.readVarInt();
        Map<M, int[]> presets = new EnumMap<>(modeClass);
        for (int i = 0; i < size; i++) {
            M mode = buf.readEnum(modeClass);
            int baseline = buf.readVarInt();
            int projectedMax = buf.readVarInt();
            presets.put(mode, new int[]{baseline, projectedMax});
        }
        return presets;
    }

    public static ConfigReloadPacket decode(FriendlyByteBuf buf) {
        int healthBaseline = buf.readVarInt();
        int healthProjectedMax = buf.readVarInt();

        int staminaBaseline = buf.readVarInt();
        int staminaProjectedMax = buf.readVarInt();
        Map<StaminaSourceMode, int[]> staminaPresets = readPresets(buf, StaminaSourceMode.class);

        int manaBaseline = buf.readVarInt();
        int manaProjectedMax = buf.readVarInt();
        Map<ManaSourceMode, int[]> manaPresets = readPresets(buf, ManaSourceMode.class);

        return new ConfigReloadPacket(
                healthBaseline, healthProjectedMax,
                staminaBaseline, staminaProjectedMax, staminaPresets,
                manaBaseline, manaProjectedMax, manaPresets
        );
    }

    public static void handle(ConfigReloadPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            SoulsCombatHUDConfig.StatsData stats = SoulsCombatHUDConfig.STATS_DATA;

            stats.health.baseline.set(packet.healthBaseline);
            stats.health.projectedMax.set(packet.healthProjectedMax);

            stats.stamina.baseline.set(packet.staminaBaseline);
            stats.stamina.projectedMax.set(packet.staminaProjectedMax);
            applyPresets(stats.stamina.presets, packet.staminaPresets);

            stats.mana.baseline.set(packet.manaBaseline);
            stats.mana.projectedMax.set(packet.manaProjectedMax);
            applyPresets(stats.mana.presets, packet.manaPresets);
        });
        ctx.setPacketHandled(true);
    }

    private static <M extends Enum<M>> void applyPresets(
            Map<M, SoulsCombatHUDConfig.StatThreshold> presets, Map<M, int[]> incoming) {
        for (Map.Entry<M, int[]> entry : incoming.entrySet()) {
            SoulsCombatHUDConfig.StatThreshold preset = presets.get(entry.getKey());
            if (preset == null) continue;
            preset.baseline.set(entry.getValue()[0]);
            preset.projectedMax.set(entry.getValue()[1]);
        }
    }
}