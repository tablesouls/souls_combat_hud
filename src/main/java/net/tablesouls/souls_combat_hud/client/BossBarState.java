package net.tablesouls.souls_combat_hud.client;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public class BossBarState {
    private static final Map<UUID, Entry> ACTIVE = new LinkedHashMap<>();
    private static final Set<UUID> seenThisFrame = new HashSet<>();

    public static void beginFrame() {
        seenThisFrame.clear();
    }

    public static void update(UUID id, Component name, float progress) {
        Entry entry = ACTIVE.computeIfAbsent(id, k -> new Entry());
        entry.name = name;
        entry.progress = progress;
        seenThisFrame.add(id);
    }

    public static boolean isActiveThisFrame(UUID id) {
        return seenThisFrame.contains(id);
    }

    public static Map<UUID, Entry> getActive() {
        return ACTIVE;
    }

    public static class Entry {
        public volatile Component name;
        public volatile float progress;
    }
}