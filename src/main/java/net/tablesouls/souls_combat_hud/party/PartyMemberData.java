package net.tablesouls.souls_combat_hud.party;

import net.minecraft.server.level.ServerPlayer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class PartyMemberData {
    public static final Map<UUID, PartyMemberData> PLAYERS = new HashMap<>();

    private WeakReference<ServerPlayer> serverPlayer;
    private final Map<PartyStatType, Object> stats = new HashMap<>();

    public static PartyMemberData get(UUID id) {
        return PLAYERS.computeIfAbsent(id, k -> new PartyMemberData());
    }

    public static void remove(UUID id) {
        PLAYERS.remove(id);
    }

    public void setServerPlayer(ServerPlayer player) {
        this.serverPlayer = new WeakReference<>(player);
    }

    public void clearServerPlayer() {
        this.serverPlayer = null;
    }

    public ServerPlayer getServerPlayer() {
        return serverPlayer != null ? serverPlayer.get() : null;
    }

    public <T> void setStat(PartyStatType type, T value, Consumer<T> onChange) {
        boolean changed = !stats.containsKey(type) || !stats.get(type).equals(value);
        stats.put(type, value);
        if (changed) {
            onChange.accept(value);
        }
    }

    public void clearStat(PartyStatType type) {
        stats.remove(type);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStat(PartyStatType type) {
        return (T) stats.getOrDefault(type, type.empty());
    }

    public boolean hasStat(PartyStatType type) {
        return stats.containsKey(type);
    }
}