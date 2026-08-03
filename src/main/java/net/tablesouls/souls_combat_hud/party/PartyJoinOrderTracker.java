package net.tablesouls.souls_combat_hud.party;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class PartyJoinOrderTracker {
    private static final Gson GSON = new Gson();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("soulscombathud").resolve("party_join_order.json");
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Long>>() {}.getType();

    private static final Map<UUID, Long> JOIN_TIME = new HashMap<>();
    private static boolean loaded = false;

    private PartyJoinOrderTracker() {}

    public static synchronized List<UUID> sortNewestFirst(List<UUID> teammates) {
        loadIfNeeded();

        boolean changed = false;
        for (UUID id : teammates) {
            if (!JOIN_TIME.containsKey(id)) {
                JOIN_TIME.put(id, System.currentTimeMillis());
                changed = true;
            }
        }
        if (changed) save();

        List<UUID> sorted = new ArrayList<>(teammates);
        sorted.sort(Comparator.comparingLong((UUID id) -> JOIN_TIME.getOrDefault(id, 0L)).reversed());
        return sorted;
    }

    private static void loadIfNeeded() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;

        try (var reader = Files.newBufferedReader(FILE)) {
            Map<String, Long> raw = GSON.fromJson(reader, MAP_TYPE);
            if (raw != null) {
                raw.forEach((k, v) -> {
                    try {
                        JOIN_TIME.put(UUID.fromString(k), v);
                    } catch (IllegalArgumentException ignored) {
                        // corrupt/foreign entry, skip it
                    }
                });
            }
        } catch (IOException e) {
            // missing/corrupt file - just start fresh
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, Long> raw = new HashMap<>();
            JOIN_TIME.forEach((k, v) -> raw.put(k.toString(), v));
            Files.writeString(FILE, GSON.toJson(raw));
        } catch (IOException e) {

        }
    }
}