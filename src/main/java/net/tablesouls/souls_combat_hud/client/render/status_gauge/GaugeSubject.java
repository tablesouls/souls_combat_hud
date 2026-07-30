package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface GaugeSubject {
    Component getDisplayName();
    ResourceLocation getSkinTexture();
    boolean isDeadOrDying();

    default boolean isOnline() {
        return true;
    }

    float getHealth();
    float getMaxHealth();

    default boolean hasHealthData() {
        return true;
    }

    boolean hasStamina();
    float getStamina();
    float getMaxStamina();

    boolean hasMana();
    float getMana();
    float getMaxMana();

    OptionalInt getFoodLevel();
    OptionalInt getArmorValue();

    Optional<AbstractClientPlayer> asRenderableEntity();

    List<MobEffectInstance> getStatusEffects();
}