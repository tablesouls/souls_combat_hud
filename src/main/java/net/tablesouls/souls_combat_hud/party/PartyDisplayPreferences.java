package net.tablesouls.souls_combat_hud.party;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Stores per-client display preferences for party gauges: pinned members,
 * hidden members, and a manual reorder index. Purely cosmetic/local - never
 * synced to other players. Persisted to disk the same way as
 * {@link PartyJoinOrderTracker} so it survives a game restart.
 */
public final class PartyDisplayPreferences {
    private static final Gson GSON = new Gson();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("soulscombathud").resolve("party_display_prefs.json");
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, Entry>>() {}.getType();

    // Mirrors PartyJoinOrderTracker's on-disk shape: UUID string -> Entry.
    // customOrder is null until the player manually reorders that member;
    // members without an explicit order fall back to join-order in the caller.
    private static final class Entry {
        boolean pinned;
        boolean hidden;
        Integer customOrder;
    }

    private static final Map<UUID, Entry> PREFS = new HashMap<>();
    private static boolean loaded = false;

    // Bumped on every mutation. Callers that recompute a derived, sorted
    // view of the roster (e.g. TeamProviderRegistry.resolveDisplayedTeammateIds,
    // which used to run its full filter+sort every render frame) can cache
    // their result against this and skip recomputing when it hasn't moved.
    private static volatile long version = 0;

    private PartyDisplayPreferences() {}

    public static long version() {
        return version;
    }

    public static synchronized boolean isPinned(UUID id) {
        loadIfNeeded();
        Entry e = PREFS.get(id);
        return e != null && e.pinned;
    }

    public static synchronized boolean isHidden(UUID id) {
        loadIfNeeded();
        Entry e = PREFS.get(id);
        return e != null && e.hidden;
    }

    /** Null if the member has never been manually reordered. */
    public static synchronized Integer getCustomOrder(UUID id) {
        loadIfNeeded();
        Entry e = PREFS.get(id);
        return e != null ? e.customOrder : null;
    }

    public static synchronized void setPinned(UUID id, boolean pinned) {
        loadIfNeeded();
        entryFor(id).pinned = pinned;
        version++;
        save();
    }

    public static synchronized void setHidden(UUID id, boolean hidden) {
        loadIfNeeded();
        entryFor(id).hidden = hidden;
        version++;
        save();
    }

    public static synchronized void setCustomOrder(UUID id, int index) {
        loadIfNeeded();
        entryFor(id).customOrder = index;
        version++;
        save();
    }

    /**
     * Moves `id` one step earlier/later among the given roster and persists
     * new sequential customOrder values (0..n-1) for the whole roster, so
     * everyone has an explicit position after this call. Call this from the
     * screen's up/down buttons with the currently-displayed ordered list.
     */
    public static synchronized void move(List<UUID> currentOrder, UUID id, int delta) {
        loadIfNeeded();
        List<UUID> working = new ArrayList<>(currentOrder);
        int from = working.indexOf(id);
        if (from < 0) return;

        int to = Math.max(0, Math.min(working.size() - 1, from + delta));
        if (to == from) return;

        working.remove(from);
        working.add(to, id);

        for (int i = 0; i < working.size(); i++) {
            entryFor(working.get(i)).customOrder = i;
        }
        version++;
        save();
    }

    public static synchronized void clear(UUID id) {
        loadIfNeeded();
        PREFS.remove(id);
        version++;
        save();
    }

    private static Entry entryFor(UUID id) {
        return PREFS.computeIfAbsent(id, k -> new Entry());
    }

    private static void loadIfNeeded() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(FILE)) return;

        try (var reader = Files.newBufferedReader(FILE)) {
            Map<String, Entry> raw = GSON.fromJson(reader, MAP_TYPE);
            if (raw != null) {
                raw.forEach((k, v) -> {
                    try {
                        PREFS.put(UUID.fromString(k), v);
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
            Map<String, Entry> raw = new HashMap<>();
            PREFS.forEach((k, v) -> raw.put(k.toString(), v));
            Files.writeString(FILE, GSON.toJson(raw));
        } catch (IOException e) {
            // best-effort persistence; a failed write just means prefs
            // reset next launch, not a crash
        }
    }
}