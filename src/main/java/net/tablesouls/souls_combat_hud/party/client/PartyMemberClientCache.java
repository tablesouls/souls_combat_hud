package net.tablesouls.souls_combat_hud.party.client;

import net.tablesouls.souls_combat_hud.party.PartyStatType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyMemberClientCache {
    private static final Map<UUID, Map<PartyStatType, Object>> CACHE = new HashMap<>();
    private static final Map<UUID, Integer> STATUS_EFFECTS_RECEIVED_TICK = new HashMap<>();
    private static final Set<UUID> HIDDEN = ConcurrentHashMap.newKeySet();

    public static void update(UUID player, PartyStatType type, Object value) {
        CACHE.computeIfAbsent(player, k -> new EnumMap<>(PartyStatType.class)).put(type, value);
        if (type == PartyStatType.STATUS_EFFECTS) {
            STATUS_EFFECTS_RECEIVED_TICK.put(player, currentClientTick());
        }
    }

    public static int getStatusEffectsReceivedTick(UUID player) {
        return STATUS_EFFECTS_RECEIVED_TICK.getOrDefault(player, currentClientTick());
    }

    private static int currentClientTick() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        return level != null ? (int) level.getGameTime() : 0;
    }

    public static void remove(UUID player, PartyStatType type) {
        Map<PartyStatType, Object> stats = CACHE.get(player);
        if (stats != null) {
            stats.remove(type);
        }
        if (type == PartyStatType.STATUS_EFFECTS) {
            STATUS_EFFECTS_RECEIVED_TICK.remove(player);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(UUID player, PartyStatType type) {
        Map<PartyStatType, Object> stats = CACHE.get(player);
        return stats != null ? (T) stats.getOrDefault(type, type.empty()) : (T) type.empty();
    }

    public static boolean has(UUID player, PartyStatType type) {
        Map<PartyStatType, Object> stats = CACHE.get(player);
        return stats != null && stats.containsKey(type);
    }

    public static void setHidden(UUID player, boolean hidden) {
        if (hidden) {
            HIDDEN.add(player);
            CACHE.remove(player);
        } else {
            HIDDEN.remove(player);
        }
    }

    public static boolean isHidden(UUID player) {
        return HIDDEN.contains(player);
    }

    public static void clearAll() {
        CACHE.clear();
        STATUS_EFFECTS_RECEIVED_TICK.clear();
        HIDDEN.clear();
    }
}