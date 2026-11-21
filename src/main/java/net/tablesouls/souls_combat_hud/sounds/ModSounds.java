package net.tablesouls.souls_combat_hud.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;

public class ModSounds {
    public static final SoundEvent CYCLE_WEAPON =
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_weapon"));
    public static final SoundEvent CYCLE_OFFHAND =
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_offhand"));
    public static final SoundEvent CYCLE_CONSUMABLE =
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_consumable"));
    public static final SoundEvent CYCLE_SPELL =
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_spell"));
}
