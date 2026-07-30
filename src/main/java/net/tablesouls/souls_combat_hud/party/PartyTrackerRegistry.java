package net.tablesouls.souls_combat_hud.party;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyTrackerRegistry {
    private static final Map<UUID, Set<UUID>> TRACKERS = new HashMap<>();  // toTrack -> hosts tracking them
    private static final Map<UUID, Set<UUID>> TRACKING = new HashMap<>();  // trackerHost -> subjects they track

    public static void addTracker(UUID toTrack, UUID trackerHost) {
        TRACKERS.computeIfAbsent(toTrack, k -> ConcurrentHashMap.newKeySet()).add(trackerHost);
        TRACKING.computeIfAbsent(trackerHost, k -> ConcurrentHashMap.newKeySet()).add(toTrack);
    }

    public static void removeTracker(UUID toTrack, UUID trackerHost) {
        Set<UUID> hosts = TRACKERS.get(toTrack);
        if (hosts != null) {
            hosts.remove(trackerHost);
            if (hosts.isEmpty()) TRACKERS.remove(toTrack);
        }
        Set<UUID> subjects = TRACKING.get(trackerHost);
        if (subjects != null) {
            subjects.remove(toTrack);
            if (subjects.isEmpty()) TRACKING.remove(trackerHost);
        }
    }

    public static Set<UUID> getTrackers(UUID toTrack) {
        return TRACKERS.getOrDefault(toTrack, Set.of());
    }

    public static void clearAllTrackersFor(UUID player) {
        Set<UUID> subjects = TRACKING.remove(player); // who 'player' was tracking
        if (subjects != null) {
            for (UUID subject : subjects) {
                Set<UUID> hosts = TRACKERS.get(subject);
                if (hosts != null) {
                    hosts.remove(player);
                    if (hosts.isEmpty()) TRACKERS.remove(subject);
                }
            }
        }

        Set<UUID> hosts = TRACKERS.remove(player); // who was tracking 'player'
        if (hosts != null) {
            for (UUID host : hosts) {
                Set<UUID> subjectsOfHost = TRACKING.get(host);
                if (subjectsOfHost != null) {
                    subjectsOfHost.remove(player);
                    if (subjectsOfHost.isEmpty()) TRACKING.remove(host);
                }
            }
        }
    }
}