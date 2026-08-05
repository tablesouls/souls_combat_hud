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

    private static final Set<UUID> HIDDEN = ConcurrentHashMap.newKeySet();

    public static void update(UUID player, PartyStatType type, Object value) {
        CACHE.computeIfAbsent(player, k -> new EnumMap<>(PartyStatType.class)).put(type, value);
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

    public static void clear(UUID player) {
        CACHE.remove(player);
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
        HIDDEN.clear();
    }
}