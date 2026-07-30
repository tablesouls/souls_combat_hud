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
import java.util.Map;
import java.util.UUID;

public final class PartyMemberProfileCache {
    private static final Gson GSON = new Gson();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve(SoulsCombatHUD.MODID)
            .resolve("party_names.json");

    private static final int MAX_ENTRIES = 200;

    private static final Map<UUID, String> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, String> eldest) {
            return size() > MAX_ENTRIES;
        }
    };
    private static boolean loaded = false;
    private static boolean dirty = false;

    private PartyMemberProfileCache() {}

    public static void remember(UUID playerId, Component displayName) {
        load();
        String name = displayName.getString();
        if (name.equals(CACHE.get(playerId))) return;
        CACHE.put(playerId, name);
        dirty = true;
    }

    public static Component getDisplayName(UUID playerId) {
        load();
        String name = CACHE.get(playerId);
        return name != null ? Component.literal(name) : null;
    }

    public static void clear(UUID playerId) {
        load();
        if (CACHE.remove(playerId) != null) {
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
            Map<String, String> raw = GSON.fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            if (raw != null) {
                raw.forEach((id, name) -> CACHE.put(UUID.fromString(id), name));
            }
        } catch (IOException | RuntimeException e) {
            SoulsCombatHUD.LOGGER.warn("Failed to load party name cache", e);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, String> raw = new HashMap<>();
            CACHE.forEach((id, name) -> raw.put(id.toString(), name));
            Files.writeString(FILE, GSON.toJson(raw));
        } catch (IOException e) {
            SoulsCombatHUD.LOGGER.warn("Failed to save party name cache", e);
        }
    }
}