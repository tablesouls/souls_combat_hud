package net.tablesouls.souls_combat_hud.party;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PartyMemberProfileCache {
    private static final Gson GSON = new Gson();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve(SoulsCombatHUD.MODID)
            .resolve("party_names.json");

    private static final int MAX_ENTRIES = 200;

    private record Entry(String username, String displayName) {}

    // UUID -> {username, displayName}, LRU-capped, access-ordered
    private static final Map<UUID, Entry> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Entry> eldest) {
            boolean evict = size() > MAX_ENTRIES;
            if (evict) NAME_INDEX.remove(eldest.getValue().username().toLowerCase(Locale.ROOT));
            return evict;
        }
    };

    // lowercase username -> UUID, rebuilt from CACHE, never persisted separately
    private static final Map<String, UUID> NAME_INDEX = new HashMap<>();

    private static boolean loaded = false;
    private static boolean dirty = false;

    private PartyMemberProfileCache() {}

    public static void remember(UUID playerId, String username, Component displayName) {
        load();
        String name = displayName.getString();
        Entry existing = CACHE.get(playerId);
        if (existing != null && existing.username().equals(username) && existing.displayName().equals(name)) {
            return;
        }
        CACHE.put(playerId, new Entry(username, name));
        NAME_INDEX.put(username.toLowerCase(Locale.ROOT), playerId);
        dirty = true;
    }

    public static Component getDisplayName(UUID playerId) {
        load();
        Entry entry = CACHE.get(playerId);
        return entry != null ? Component.literal(entry.displayName()) : null;
    }

    /** Vanilla scoreboard teams track membership by username, not UUID - this resolves offline members. */
    public static UUID getUuidForUsername(String username) {
        load();
        return NAME_INDEX.get(username.toLowerCase(Locale.ROOT));
    }

    public static void clear(UUID playerId) {
        load();
        Entry removed = CACHE.remove(playerId);
        if (removed != null) {
            NAME_INDEX.remove(removed.username().toLowerCase(Locale.ROOT));
            dirty = true;
        }
    }

    public static void flush() {
        if (!dirty) return;
        save();
        dirty = false;
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;
        try (var reader = Files.newBufferedReader(FILE)) {
            Map<String, Entry> raw = GSON.fromJson(reader, new TypeToken<Map<String, Entry>>(){}.getType());
            if (raw != null) {
                raw.forEach((id, entry) -> {
                    try {
                        UUID uuid = UUID.fromString(id);
                        CACHE.put(uuid, entry);
                        NAME_INDEX.put(entry.username().toLowerCase(Locale.ROOT), uuid);
                    } catch (IllegalArgumentException | NullPointerException ignored) {
                        // corrupt/foreign entry, skip it
                    }
                });
            }
        } catch (IOException | RuntimeException e) {
            SoulsCombatHUD.LOGGER.warn("Failed to load party name cache", e);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, Entry> raw = new HashMap<>();
            CACHE.forEach((id, entry) -> raw.put(id.toString(), entry));
            Files.writeString(FILE, GSON.toJson(raw));
        } catch (IOException e) {
            SoulsCombatHUD.LOGGER.warn("Failed to save party name cache", e);
        }
    }
}