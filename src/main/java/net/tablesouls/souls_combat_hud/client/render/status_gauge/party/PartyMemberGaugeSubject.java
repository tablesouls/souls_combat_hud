package net.tablesouls.souls_combat_hud.client.render.status_gauge.party;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;
import net.tablesouls.souls_combat_hud.accessor.IEffectDurationAccessor;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeSubject;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.party.PartyEffectSnapshot;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberClientCache;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberProfileCache;
import net.tablesouls.souls_combat_hud.party.PartyStatType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;

public class PartyMemberGaugeSubject implements GaugeSubject {
    private final UUID playerId;

    public PartyMemberGaugeSubject(UUID playerId) {
        this.playerId = playerId;
    }

    private PlayerInfo playerInfo() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null ? connection.getPlayerInfo(playerId) : null;
    }

    @Override
    public Component getDisplayName() {
        PlayerInfo info = playerInfo();
        if (info != null) {
            Component tabName = info.getTabListDisplayName();
            Component name = tabName != null ? tabName : Component.literal(info.getProfile().getName());
            PartyMemberProfileCache.remember(playerId, info.getProfile().getName(), name);
            return name;
        }

        Component cached = PartyMemberProfileCache.getDisplayName(playerId);
        return cached != null ? cached : Component.literal(playerId.toString());
    }

    @Override
    public ResourceLocation getSkinTexture() {
        PlayerInfo info = playerInfo();
        return info != null ? info.getSkinLocation() : null;
    }

    @Override
    public boolean isDeadOrDying() {
        return hasHealthData() && this.<Float>getStat(PartyStatType.HEALTH) <= 0.0f;
    }

    @Override
    public boolean isOnline() {
        return playerInfo() != null;
    }

    private <T> T getStat(PartyStatType type) {
        return PartyMemberClientCache.get(playerId, type);
    }

    @Override
    public boolean hasHealthData() {
        return PartyMemberClientCache.has(playerId, PartyStatType.HEALTH)
                && PartyMemberClientCache.has(playerId, PartyStatType.MAX_HEALTH);
    }

    @Override
    public float getHealth() {
        return PartyMemberClientCache.get(playerId, PartyStatType.HEALTH);
    }

    @Override
    public float getMaxHealth() {
        return PartyMemberClientCache.get(playerId, PartyStatType.MAX_HEALTH);
    }

    @Override
    public boolean hasStamina() {
        return getMaxStamina() > 0;
    }

    @Override
    public float getStamina() {
        return PartyMemberClientCache.get(playerId, PartyStatType.STAMINA);
    }

    @Override
    public float getMaxStamina() {
        return PartyMemberClientCache.get(playerId, PartyStatType.MAX_STAMINA);
    }

    @Override
    public StaminaSourceMode getStaminaSourceMode() {
        return parseMode(PartyMemberClientCache.get(playerId, PartyStatType.STAMINA_MODE), StaminaSourceMode.class);
    }

    @Override
    public boolean hasMana() {
        return getMaxMana() > 0;
    }

    @Override
    public float getMana() {
        return PartyMemberClientCache.get(playerId, PartyStatType.MANA);
    }

    @Override
    public float getMaxMana() {
        return PartyMemberClientCache.get(playerId, PartyStatType.MAX_MANA);
    }

    @Override
    public ManaSourceMode getManaSourceMode() {
        return parseMode(PartyMemberClientCache.get(playerId, PartyStatType.MANA_MODE), ManaSourceMode.class);
    }

    private static <M extends Enum<M>> M parseMode(String name, Class<M> type) {
        if (name == null || name.isEmpty()) return null;
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public OptionalInt getFoodLevel() {
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt getArmorValue() {
        return OptionalInt.empty();
    }

    @Override
    public Optional<Boolean> hasThirst() {
        return Optional.of(false);
    }

    @Override
    public OptionalDouble getThirst() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMaxThirst() {
        return OptionalDouble.empty();
    }

    @Override
    public Optional<AbstractClientPlayer> asRenderableEntity() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return level.players().stream()
                .filter(p -> p.getUUID().equals(playerId))
                .findFirst();
    }

    @Override
    public List<MobEffectInstance> getStatusEffects() {
        List<PartyEffectSnapshot> snapshots = PartyMemberClientCache.get(playerId, PartyStatType.STATUS_EFFECTS);
        int receivedTick = PartyMemberClientCache.getStatusEffectsReceivedTick(playerId);
        int currentTick = (int) Minecraft.getInstance().level.getGameTime();
        int elapsed = Math.max(0, currentTick - receivedTick);

        List<MobEffectInstance> instances = new ArrayList<>(snapshots.size());
        for (PartyEffectSnapshot snapshot : snapshots) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(snapshot.effectId());
            if (effect == null) continue;

            boolean infinite = snapshot.duration() < 0;
            int remaining = infinite ? -1 : snapshot.duration() - elapsed;
            if (!infinite && remaining <= 0) continue;

            MobEffectInstance instance = new MobEffectInstance(effect, remaining, snapshot.amplifier());
            ((IEffectDurationAccessor) instance).souls_combat_hud$setMaxDuration(snapshot.maxDuration());
            instances.add(instance);
        }
        return instances;
    }
}