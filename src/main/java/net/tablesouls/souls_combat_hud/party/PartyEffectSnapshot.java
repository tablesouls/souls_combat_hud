package net.tablesouls.souls_combat_hud.party;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record PartyEffectSnapshot(ResourceLocation effectId, int amplifier, int duration, int maxDuration) {
    public PartyEffectSnapshot {
        Objects.requireNonNull(effectId, "effectId");
    }
}