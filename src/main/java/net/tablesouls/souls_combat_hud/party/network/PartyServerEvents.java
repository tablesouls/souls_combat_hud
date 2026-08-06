package net.tablesouls.souls_combat_hud.party.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.AbstractResourceSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightDodgeListener;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;
import net.tablesouls.souls_combat_hud.party.PartyEffectSnapshot;
import net.tablesouls.souls_combat_hud.party.PartyMemberData;
import net.tablesouls.souls_combat_hud.party.PartyStatType;
import net.tablesouls.souls_combat_hud.party.server.PartyMembershipRegistry;
import net.tablesouls.souls_combat_hud.party.server.PartyPrivacyRegistry;
import net.tablesouls.souls_combat_hud.party.server.PartyTrackerRegistry;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID)
public class PartyServerEvents {
    private static final Map<UUID, Set<UUID>> LAST_TEAMMATES = new HashMap<>();
    private static final Map<UUID, TeamSourceMode> LAST_TEAM_MODE = new HashMap<>();
    private static final Map<UUID, ManaSourceMode> LAST_MANA_MODE = new HashMap<>();
    private static final Map<UUID, StaminaSourceMode> LAST_STAMINA_MODE = new HashMap<>();

    private static final Map<UUID, Integer> LAST_EFFECT_PUSH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_HEALTH_PUSH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_STATUS_EFFECT_PUSH_TICK = new HashMap<>();

    private static final Map<UUID, Boolean> RESOURCE_MAX_PENDING = new HashMap<>();
    private static final Map<UUID, Boolean> HEALTH_PENDING = new HashMap<>();
    private static final Map<UUID, Boolean> STATUS_EFFECT_PENDING = new HashMap<>();

    private static final Map<UUID, Boolean> LAST_PARTY_TRACKING_HANDSHAKE = new HashMap<>();

    private static final Map<UUID, Map<ResourceLocation, Integer>> EFFECT_MAX_DURATION = new HashMap<>();

    private static Boolean lastHealthTrackingDisabled = null;
    private static Boolean lastStaminaTrackingDisabled = null;
    private static Boolean lastManaTrackingDisabled = null;
    private static Boolean lastStatusEffectTrackingDisabled = null;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PartyMemberData.get(player.getUUID()).setServerPlayer(player);
        recomputeTrackersFor(player);
        resolveAndSyncResourceSources(player);

        if (EpicFightCompat.LOADED) {
            EpicFightDodgeListener.register(player);
        }

        PartyNetwork.sendFeatureHandshake(player, partyTrackingEnabled());
    }

    private static boolean partyTrackingEnabled() {
        return !SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disablePartyTracking.get();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 100 != 0) return; // every 5 seconds

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            recomputeTrackersFor(player);
        }
    }

    public static void checkTrackingToggles(MinecraftServer server) {
        boolean healthDisabled = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get();
        boolean staminaDisabled = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStaminaTracking.get();
        boolean manaDisabled = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableManaTracking.get();
        boolean statusEffectDisabled = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStatusEffectTracking.get();

        if (justDisabled(lastHealthTrackingDisabled, healthDisabled)) {
            clearStatForAllPlayers(server, PartyStatType.HEALTH, PartyStatType.MAX_HEALTH);
        }
        if (justDisabled(lastStaminaTrackingDisabled, staminaDisabled)) {
            clearStatForAllPlayers(server, PartyStatType.STAMINA, PartyStatType.MAX_STAMINA, PartyStatType.STAMINA_MODE);
        }
        if (justDisabled(lastManaTrackingDisabled, manaDisabled)) {
            clearStatForAllPlayers(server, PartyStatType.MANA, PartyStatType.MAX_MANA, PartyStatType.MANA_MODE);
        }
        if (justDisabled(lastStatusEffectTrackingDisabled, statusEffectDisabled)) {
            clearStatForAllPlayers(server, PartyStatType.STATUS_EFFECTS);
        }

        lastHealthTrackingDisabled = healthDisabled;
        lastStaminaTrackingDisabled = staminaDisabled;
        lastManaTrackingDisabled = manaDisabled;
        lastStatusEffectTrackingDisabled = statusEffectDisabled;
    }

    private static boolean justDisabled(Boolean previous, boolean current) {
        return current && (previous == null || !previous);
    }

    private static void clearStatForAllPlayers(MinecraftServer server, PartyStatType... types) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (PartyStatType type : types) {
                clearStatForTrackers(player, type);
            }
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String dimensionId = event.getTo().location().toString();
        PartyMemberData.get(player.getUUID())
                .setStat(PartyStatType.DIMENSION, dimensionId, v -> broadcast(player, PartyStatType.DIMENSION, v));
    }

    public static void recomputeTrackersFor(ServerPlayer player) {
        UUID id = player.getUUID();
        PartyTrackerRegistry.clearAllTrackersFor(id); // wipe both directions, then rebuild from current

        boolean enabled = partyTrackingEnabled();
        Boolean lastNotified = LAST_PARTY_TRACKING_HANDSHAKE.put(id, enabled);
        if (lastNotified == null || lastNotified != enabled) {
            PartyNetwork.sendFeatureHandshake(player, enabled);
        }

        // If party tracking is disabled server side, leave trackers empty
        if (!enabled) {
            LAST_TEAMMATES.remove(id);
            LAST_TEAM_MODE.remove(id);
            return;
        }

        PartyMembershipRegistry.Resolution resolution = PartyMembershipRegistry.resolve(player);
        List<UUID> resolvedTeammates = resolution.teammateIds();
        int maxTracked = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.maxTrackedPartyMembers.get();

        Set<UUID> newTeammates;
        if (maxTracked > 0 && resolvedTeammates.size() > maxTracked) {
            newTeammates = resolvedTeammates.stream()
                    .sorted(Comparator.comparing(UUID::toString)) // do not shuffle every recompute
                    .limit(maxTracked)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            newTeammates = new HashSet<>(resolvedTeammates);
        }

        Set<UUID> previousTeammates = LAST_TEAMMATES.getOrDefault(id, Set.of());

        // Tell the client which source we resolved only when changed
        TeamSourceMode previousMode = LAST_TEAM_MODE.get(id);
        if (previousMode != resolution.mode()) {
            PartyNetwork.sendTeamSource(player, resolution.mode());
            LAST_TEAM_MODE.put(id, resolution.mode());
        }

        for (UUID teammateId : newTeammates) {
            // player tracks teammate vice versa
            PartyTrackerRegistry.addTracker(teammateId, id);
            PartyTrackerRegistry.addTracker(id, teammateId);

            if (!previousTeammates.contains(teammateId)) {
                ServerPlayer teammate = player.getServer().getPlayerList().getPlayer(teammateId);
                if (teammate != null) {
                    syncKnownStats(teammate, player);
                    syncKnownStats(player, teammate);
                }
            }
        }

        LAST_TEAMMATES.put(id, newTeammates);
    }

    static void syncKnownStats(ServerPlayer subject, ServerPlayer to) {
        boolean hidden = PartyPrivacyRegistry.isHidden(subject.getUUID());
        PartyNetwork.sendPrivacyState(to, subject.getUUID(), hidden);
        if (hidden) return;

        PartyMemberData data = PartyMemberData.get(subject.getUUID());
        for (PartyStatType type : PartyStatType.values()) {
            if (data.hasStat(type)) {
                PartyNetwork.sendStatUpdate(to, subject.getUUID(), type, data.getStat(type));
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        PartyMemberData.remove(id);
        PartyTrackerRegistry.clearAllTrackersFor(id);
        LAST_TEAMMATES.remove(id);
        LAST_TEAM_MODE.remove(id);
        LAST_MANA_MODE.remove(id);
        LAST_STAMINA_MODE.remove(id);
        EFFECT_MAX_DURATION.remove(id);
        RESOURCE_MAX_PENDING.remove(id);
        HEALTH_PENDING.remove(id);
        STATUS_EFFECT_PENDING.remove(id);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PartyMemberData.get(player.getUUID()).setServerPlayer(player);
        if (event.isWasDeath()) {
            pushResourceMax(player);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        pushHealth(player, Math.max(player.getHealth() - event.getAmount(), 0.0f));
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        pushHealth(player, Math.min(player.getHealth() + event.getAmount(), player.getMaxHealth()));
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        pushStatusEffects(player);

        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnMobEffect.get()) return;
        pushResourceMax(player);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEffectInstance() == null) return;
        pushStatusEffects(player);

        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnMobEffect.get()) return;
        pushResourceMax(player);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEffectInstance() == null) return;
        pushStatusEffects(player);

        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnMobEffect.get()) return;
        pushResourceMax(player);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnEquipmentChange.get()) return;
        if (event.getSlot().getType() != EquipmentSlot.Type.ARMOR) return; // ignore hand/offhand swaps
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        pushResourceMax(player);
    }

    private record ResolvedResources(
            AbstractResourceSourceRegistry.Resolution<ManaSourceMode> mana,
            AbstractResourceSourceRegistry.Resolution<StaminaSourceMode> stamina) {
    }

    private static ResolvedResources resolveAndSyncResourceSources(ServerPlayer player) {
        UUID id = player.getUUID();

        ManaSourceMode forcedMana = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.forceManaSource.get();
        StaminaSourceMode forcedStamina = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.forceStaminaSource.get();

        AbstractResourceSourceRegistry.Resolution<ManaSourceMode> manaResolution =
                ManaSourceRegistry.INSTANCE.resolveServerSide(player, forcedMana);
        AbstractResourceSourceRegistry.Resolution<StaminaSourceMode> staminaResolution =
                StaminaSourceRegistry.INSTANCE.resolveServerSide(player, forcedStamina);

        if (LAST_MANA_MODE.get(id) != manaResolution.mode()) {
            PartyNetwork.sendManaSource(player, manaResolution.mode());
            LAST_MANA_MODE.put(id, manaResolution.mode());

            if (manaResolution.mode() != null) {
                PartyMemberData.get(id).setStat(PartyStatType.MANA_MODE, manaResolution.mode().name(),
                        v -> broadcast(player, PartyStatType.MANA_MODE, v));
            } else {
                clearStatForTrackers(player, PartyStatType.MANA_MODE);
            }
        }
        if (LAST_STAMINA_MODE.get(id) != staminaResolution.mode()) {
            PartyNetwork.sendStaminaSource(player, staminaResolution.mode());
            LAST_STAMINA_MODE.put(id, staminaResolution.mode());

            if (staminaResolution.mode() != null) {
                PartyMemberData.get(id).setStat(PartyStatType.STAMINA_MODE, staminaResolution.mode().name(),
                        v -> broadcast(player, PartyStatType.STAMINA_MODE, v));
            } else {
                clearStatForTrackers(player, PartyStatType.STAMINA_MODE);
            }
        }

        return new ResolvedResources(manaResolution, staminaResolution);
    }

    private static boolean shouldThrottle(UUID id, Map<UUID, Integer> lastPushTicks, Map<UUID, Boolean> pending,
                                          int now, int minTicks) {
        Integer last = lastPushTicks.get(id);
        if (last != null && now - last < minTicks) {
            pending.put(id, true);
            return true;
        }
        lastPushTicks.put(id, now);
        pending.remove(id);
        return false;
    }

    public static void pushResourceMax(ServerPlayer player) {
        UUID id = player.getUUID();
        int now = player.tickCount;
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minInstantUpdateTicks.get();
        if (shouldThrottle(id, LAST_EFFECT_PUSH_TICK, RESOURCE_MAX_PENDING, now, minTicks)) return;

        PartyMemberData data = PartyMemberData.get(id);

        ResolvedResources resources = resolveAndSyncResourceSources(player);
        ResourceSource<StaminaSourceMode> stamina = resources.stamina().source();
        ResourceSource<ManaSourceMode> mana = resources.mana().source();

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get()) {
            data.setStat(PartyStatType.HEALTH, player.getHealth(), v -> broadcast(player, PartyStatType.HEALTH, v));
            data.setStat(PartyStatType.MAX_HEALTH, player.getMaxHealth(), v -> broadcast(player, PartyStatType.MAX_HEALTH, v));
        }

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStaminaTracking.get()) {
            if (stamina != null) {
                data.setStat(PartyStatType.STAMINA, stamina.getCurrent(player), v -> broadcast(player, PartyStatType.STAMINA, v));
                data.setStat(PartyStatType.MAX_STAMINA, stamina.getMax(player), v -> broadcast(player, PartyStatType.MAX_STAMINA, v));
            }
        }

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableManaTracking.get()) {
            if (mana != null) {
                data.setStat(PartyStatType.MANA, mana.getCurrent(player), v -> broadcast(player, PartyStatType.MANA, v));
                data.setStat(PartyStatType.MAX_MANA, mana.getMax(player), v -> broadcast(player, PartyStatType.MAX_MANA, v));
            }
        }
    }

    public static void pushStatusEffects(ServerPlayer player) {
        if (SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStatusEffectTracking.get()) return;

        UUID id = player.getUUID();
        int now = player.tickCount;
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minInstantUpdateTicks.get();
        if (shouldThrottle(id, LAST_STATUS_EFFECT_PUSH_TICK, STATUS_EFFECT_PENDING, now, minTicks)) return;

        Map<ResourceLocation, Integer> maxDurations = EFFECT_MAX_DURATION.computeIfAbsent(id, k -> new HashMap<>());
        Set<ResourceLocation> currentIds = new HashSet<>();

        List<PartyEffectSnapshot> snapshots = player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .map(instance -> {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());
                    if (effectId == null) return null;
                    currentIds.add(effectId);
                    int duration = instance.getDuration();
                    int maxDuration = maxDurations.merge(effectId, duration, Math::max);
                    return new PartyEffectSnapshot(effectId, instance.getAmplifier(), duration, maxDuration);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PartyEffectSnapshot::effectId))
                .toList();

        maxDurations.keySet().retainAll(currentIds); // drop anything no longer active

        int maxTracked = SoulsCombatHUDConfig.SERVER_RESTRICTIONS.maxTrackedStatusEffects.get();
        if (maxTracked > 0 && snapshots.size() > maxTracked) {
            snapshots = snapshots.subList(0, maxTracked);
        }

        PartyMemberData.get(id).setStat(PartyStatType.STATUS_EFFECTS, snapshots,
                v -> broadcast(player, PartyStatType.STATUS_EFFECTS, v));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        UUID id = player.getUUID();

        if (Boolean.TRUE.equals(STATUS_EFFECT_PENDING.get(id))) pushStatusEffects(player);
        if (Boolean.TRUE.equals(HEALTH_PENDING.get(id))) pushHealth(player, player.getHealth());
        if (Boolean.TRUE.equals(RESOURCE_MAX_PENDING.get(id))) pushResourceMax(player);

        if (player.tickCount % SoulsCombatHUDConfig.SERVER_PERFORMANCE.ticksInterval.get() != 0) return;
        syncTrackedStats(player);
    }

    private static void syncTrackedStats(ServerPlayer player) {
        PartyMemberData data = PartyMemberData.get(player.getUUID());

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get()) {
            data.setStat(PartyStatType.HEALTH, player.getHealth(), v -> broadcast(player, PartyStatType.HEALTH, v));
            data.setStat(PartyStatType.MAX_HEALTH, player.getMaxHealth(), v -> broadcast(player, PartyStatType.MAX_HEALTH, v));
        }

        ResolvedResources resources = resolveAndSyncResourceSources(player);

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStaminaTracking.get()) {
            ResourceSource<StaminaSourceMode> stamina = resources.stamina().source();
            if (stamina != null) {
                data.setStat(PartyStatType.STAMINA, stamina.getCurrent(player), v -> broadcast(player, PartyStatType.STAMINA, v));
                data.setStat(PartyStatType.MAX_STAMINA, stamina.getMax(player), v -> broadcast(player, PartyStatType.MAX_STAMINA, v));
            }
        }

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableManaTracking.get()) {
            ResourceSource<ManaSourceMode> mana = resources.mana().source();
            if (mana != null) {
                data.setStat(PartyStatType.MANA, mana.getCurrent(player), v -> broadcast(player, PartyStatType.MANA, v));
                data.setStat(PartyStatType.MAX_MANA, mana.getMax(player), v -> broadcast(player, PartyStatType.MAX_MANA, v));
            }
        }

        pushStatusEffects(player);
    }

    public static void forceImmediateSync(ServerPlayer player) {
        syncTrackedStats(player);
    }

    public static void clearStatForTrackers(ServerPlayer player, PartyStatType type) {
        PartyMemberData.get(player.getUUID()).clearStat(type);
        broadcast(player, type, null);
    }

    private static void pushHealth(ServerPlayer player, float health) {
        if (SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get()) return;

        UUID id = player.getUUID();
        int now = player.tickCount;
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minHealthUpdateTicks.get();
        if (shouldThrottle(id, LAST_HEALTH_PUSH_TICK, HEALTH_PENDING, now, minTicks)) return;

        PartyMemberData.get(id)
                .setStat(PartyStatType.HEALTH, health, v -> broadcast(player, PartyStatType.HEALTH, v));
    }

    private static void broadcast(ServerPlayer player, PartyStatType type, Object value) {
        if (PartyPrivacyRegistry.isHidden(player.getUUID())) return;
        for (var trackerId : PartyTrackerRegistry.getTrackers(player.getUUID())) {
            var tracker = player.getServer().getPlayerList().getPlayer(trackerId);
            if (tracker != null) {
                PartyNetwork.sendStatUpdate(tracker, player.getUUID(), type, value);
            }
        }
    }

    public static void forceResyncResourceSources(ServerPlayer player) {
        resolveAndSyncResourceSources(player);
    }
}