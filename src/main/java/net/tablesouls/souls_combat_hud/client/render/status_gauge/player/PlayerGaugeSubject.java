package net.tablesouls.souls_combat_hud.client.render.status_gauge.player;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeSubject;
import net.tablesouls.souls_combat_hud.compat.ManaProviderRegistry;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.compat.StaminaProviderRegistry;

import java.util.List;
import java.util.Optional;
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
        return StaminaProviderRegistry.resolve(player) != null;
    }

    @Override
    public float getStamina() {
        ResourceSource source = StaminaProviderRegistry.resolve(player);
        return source != null ? source.getCurrent(player) : 0.0f;
    }

    @Override
    public float getMaxStamina() {
        ResourceSource source = StaminaProviderRegistry.resolve(player);
        return source != null ? source.getMax(player) : 0.0f;
    }

    @Override
    public boolean hasMana() {
        return ManaProviderRegistry.resolve(player) != null;
    }

    @Override
    public float getMana() {
        ResourceSource source = ManaProviderRegistry.resolve(player);
        return source != null ? source.getCurrent(player) : 0.0f;
    }

    @Override
    public float getMaxMana() {
        ResourceSource source = ManaProviderRegistry.resolve(player);
        return source != null ? source.getMax(player) : 0.0f;
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