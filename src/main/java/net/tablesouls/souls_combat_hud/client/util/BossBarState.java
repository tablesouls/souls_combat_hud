package net.tablesouls.souls_combat_hud.client.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public class BossBarState {
    private static final Map<UUID, Entry> ACTIVE = new LinkedHashMap<>();
    private static long currentFrame = 0L;

    public static void beginFrame() {
        currentFrame++;
    }

    public static void update(UUID id, Component name, float progress) {
        Entry entry = ACTIVE.computeIfAbsent(id, k -> new Entry());
        entry.name = name;
        entry.progress = progress;
        entry.lastSeenFrame = currentFrame;
    }

    public static boolean isActiveThisFrame(UUID id) {
        Entry entry = ACTIVE.get(id);
        return entry != null && entry.lastSeenFrame == currentFrame;
    }

    public static Map<UUID, Entry> getActive() {
        return ACTIVE;
    }

    public static class Entry {
        public volatile Component name;
        public volatile float progress;
        public volatile float currentHealth = -1f;
        public volatile float maxHealth = -1f;
        public volatile long lastSeenFrame = -1L;
    }

    public static void updateHealth(UUID id, float current, float max) {
        Entry entry = ACTIVE.computeIfAbsent(id, k -> new Entry());
        entry.currentHealth = current;
        entry.maxHealth = max;
    }
}