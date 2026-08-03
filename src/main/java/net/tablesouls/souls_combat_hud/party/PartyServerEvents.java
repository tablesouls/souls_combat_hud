package net.tablesouls.souls_combat_hud.party;

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
import net.tablesouls.souls_combat_hud.compat.ManaProviderRegistry;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.compat.StaminaProviderRegistry;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightDodgeListener;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.TeamSourceMode;

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

    private static final Map<UUID, Integer> LAST_EFFECT_PUSH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_HEALTH_PUSH_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_STATUS_EFFECT_PUSH_TICK = new HashMap<>();

    private static final Map<UUID, Map<ResourceLocation, Integer>> EFFECT_MAX_DURATION = new HashMap<>();

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PartyMemberData.get(player.getUUID()).setServerPlayer(player);
        recomputeTrackersFor(player);

        if (EpicFightCompat.LOADED) {
            EpicFightDodgeListener.register(player);
        }

        if (partyTrackingEnabled()) {
            PartyNetwork.sendFeatureHandshake(player);
        }
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

        // If party tracking is disabled server-side, leave trackers empty
        if (!partyTrackingEnabled()) {
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

    private static void syncKnownStats(ServerPlayer subject, ServerPlayer to) {
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
        EFFECT_MAX_DURATION.remove(id);
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

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        if (effectId != null) {
            EFFECT_MAX_DURATION.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
                    .put(effectId, event.getEffectInstance().getDuration());
        }
        pushStatusEffects(player);

        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnMobEffect.get()) return;
        pushResourceMax(player);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        Map<ResourceLocation, Integer> tracked = EFFECT_MAX_DURATION.get(player.getUUID());
        if (tracked != null && effectId != null) tracked.remove(effectId);
        pushStatusEffects(player);

        if (!SoulsCombatHUDConfig.SERVER_PERFORMANCE.updateOnMobEffect.get()) return;
        pushResourceMax(player);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;

        ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        Map<ResourceLocation, Integer> tracked = EFFECT_MAX_DURATION.get(player.getUUID());
        if (tracked != null && effectId != null) tracked.remove(effectId);
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

    public static void pushResourceMax(ServerPlayer player) {
        UUID id = player.getUUID();
        int now = player.tickCount;
        Integer last = LAST_EFFECT_PUSH_TICK.get(id);
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minInstantUpdateTicks.get();
        if (last != null && now - last < minTicks) return;
        LAST_EFFECT_PUSH_TICK.put(id, now);

        PartyMemberData data = PartyMemberData.get(id);

        ResourceSource stamina = StaminaProviderRegistry.resolve(player);
        ResourceSource mana = ManaProviderRegistry.resolve(player);

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
        Integer last = LAST_STATUS_EFFECT_PUSH_TICK.get(id);
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minInstantUpdateTicks.get();
        if (last != null && now - last < minTicks) return;
        LAST_STATUS_EFFECT_PUSH_TICK.put(id, now);

        Map<ResourceLocation, Integer> maxDurations = EFFECT_MAX_DURATION.getOrDefault(id, Map.of());

        List<PartyEffectSnapshot> snapshots = player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .map(instance -> {
                    ResourceLocation effectId = ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect());
                    if (effectId == null) return null;
                    int maxDuration = maxDurations.getOrDefault(effectId, instance.getDuration());
                    return new PartyEffectSnapshot(effectId, instance.getAmplifier(), instance.getDuration(), maxDuration);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(s -> s.effectId().toString()))
                .toList();

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
        if (player.tickCount %
                SoulsCombatHUDConfig.SERVER_PERFORMANCE.ticksInterval.get() != 0) return;

        PartyMemberData data = PartyMemberData.get(player.getUUID());

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get()) {
            data.setStat(PartyStatType.HEALTH, player.getHealth(), v -> broadcast(player, PartyStatType.HEALTH, v));
            data.setStat(PartyStatType.MAX_HEALTH, player.getMaxHealth(), v -> broadcast(player, PartyStatType.MAX_HEALTH, v));
        }

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableStaminaTracking.get()) {
            ResourceSource stamina = StaminaProviderRegistry.resolve(player);
            if (stamina != null) {
                data.setStat(PartyStatType.STAMINA, stamina.getCurrent(player), v -> broadcast(player, PartyStatType.STAMINA, v));
                data.setStat(PartyStatType.MAX_STAMINA, stamina.getMax(player), v -> broadcast(player, PartyStatType.MAX_STAMINA, v));
            }
        }

        if (!SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableManaTracking.get()) {
            ResourceSource mana = ManaProviderRegistry.resolve(player);
            if (mana != null) {
                data.setStat(PartyStatType.MANA, mana.getCurrent(player), v -> broadcast(player, PartyStatType.MANA, v));
                data.setStat(PartyStatType.MAX_MANA, mana.getMax(player), v -> broadcast(player, PartyStatType.MAX_MANA, v));
            }
        }

        pushStatusEffects(player);
    }

    private static void pushHealth(ServerPlayer player, float health) {
        if (SoulsCombatHUDConfig.SERVER_RESTRICTIONS.disableHealthTracking.get()) return;

        UUID id = player.getUUID();
        int now = player.tickCount;
        Integer last = LAST_HEALTH_PUSH_TICK.get(id);
        int minTicks = SoulsCombatHUDConfig.SERVER_PERFORMANCE.minHealthUpdateTicks.get();
        if (last != null && now - last < minTicks) return;
        LAST_HEALTH_PUSH_TICK.put(id, now);

        PartyMemberData.get(id)
                .setStat(PartyStatType.HEALTH, health, v -> broadcast(player, PartyStatType.HEALTH, v));
    }

    private static void broadcast(ServerPlayer player, PartyStatType type, Object value) {
        for (var trackerId : PartyTrackerRegistry.getTrackers(player.getUUID())) {
            var tracker = player.getServer().getPlayerList().getPlayer(trackerId);
            if (tracker != null) {
                PartyNetwork.sendStatUpdate(tracker, player.getUUID(), type, value);
            }
        }
    }
}