package net.tablesouls.souls_combat_hud.client.render.status_gauge.player;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeSubject;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.ThirstSourceRegistry;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class PlayerGaugeSubject implements GaugeSubject {
    private final AbstractClientPlayer player;

    public PlayerGaugeSubject(AbstractClientPlayer player) {
        this.player = player;
    }

    @Override
    public Component getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    public ResourceLocation getSkinTexture() {
        return player.getSkinTextureLocation();
    }

    @Override
    public boolean isDeadOrDying() {
        return player.isDeadOrDying();
    }

    @Override
    public float getHealth() {
        return player.getHealth();
    }

    @Override
    public float getMaxHealth() {
        return player.getMaxHealth();
    }

    @Override
    public boolean hasStamina() {
        return resolveStaminaSource() != null;
    }

    @Override
    public float getStamina() {
        ResourceSource<StaminaSourceMode> source = resolveStaminaSource();
        return source != null ? source.getCurrent(player) : 0.0f;
    }

    @Override
    public float getMaxStamina() {
        ResourceSource<StaminaSourceMode> source = resolveStaminaSource();
        return source != null ? source.getMax(player) : 0.0f;
    }

    @Override
    public boolean hasMana() {
        return resolveManaSource() != null;
    }

    @Override
    public float getMana() {
        ResourceSource<ManaSourceMode> source = resolveManaSource();
        return source != null ? source.getCurrent(player) : 0.0f;
    }

    @Override
    public float getMaxMana() {
        ResourceSource<ManaSourceMode> source = resolveManaSource();
        return source != null ? source.getMax(player) : 0.0f;
    }

    private ResourceSource<StaminaSourceMode> resolveStaminaSource() {
        if (SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.ignoreServerStaminaSource.get()) {
            StaminaSourceMode localPreference = SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.clientStaminaSource.get();
            return StaminaSourceRegistry.INSTANCE.resolveServerSide(player, localPreference).source();
        }
        return StaminaSourceRegistry.INSTANCE.resolve();
    }

    private ResourceSource<ManaSourceMode> resolveManaSource() {
        if (SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.ignoreServerManaSource.get()) {
            ManaSourceMode localPreference = SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.clientManaSource.get();
            return ManaSourceRegistry.INSTANCE.resolveServerSide(player, localPreference).source();
        }
        return ManaSourceRegistry.INSTANCE.resolve();
    }

    @Override
    public boolean isPrivate() {
        return SoulsCombatHUDConfig.STATUS_GAUGE.hideStatusFromParty.get();
    }

    @Override
    public OptionalInt getFoodLevel() {
        return OptionalInt.of(player.getFoodData().getFoodLevel());
    }

    @Override
    public OptionalInt getArmorValue() {
        return OptionalInt.of(player.getArmorValue());
    }

    @Override
    public Optional<Boolean> hasThirst() {
        return Optional.of(resolveThirstSource() != null);
    }

    @Override
    public OptionalDouble getThirst() {
        ResourceSource<ThirstSourceMode> source = resolveThirstSource();
        return OptionalDouble.of(source != null ? source.getCurrent(player) : 0.0f);
    }

    @Override
    public OptionalDouble getMaxThirst() {
        ResourceSource<ThirstSourceMode> source = resolveThirstSource();
        return OptionalDouble.of(source != null ? source.getMax(player) : 0.0f);
    }

    private ResourceSource<ThirstSourceMode> resolveThirstSource() {
        ThirstSourceMode localPreference = SoulsCombatHUDConfig.STATUS_GAUGE.clientSourcePreference.clientThristSource.get();
        return ThirstSourceRegistry.INSTANCE.resolveServerSide(player, localPreference).source();
    }

    @Override
    public ThirstSourceMode getThirstSourceMode() {
        ResourceSource<ThirstSourceMode> source = resolveThirstSource();
        return source != null ? source.mode() : null;
    }

    @Override
    public Optional<AbstractClientPlayer> asRenderableEntity() {
        return Optional.of(player);
    }

    @Override
    public List<MobEffectInstance> getStatusEffects() {
        return player.getActiveEffects().stream()
                .filter(MobEffectInstance::showIcon)
                .toList();
    }
}