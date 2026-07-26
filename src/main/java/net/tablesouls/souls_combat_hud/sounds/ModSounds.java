package net.tablesouls.souls_combat_hud.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SoulsCombatHUD.MODID);

    public static final RegistryObject<SoundEvent> CYCLE_WEAPON = SOUND_EVENTS.register(
            "cycle_weapon",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_weapon"))
    );
    public static final RegistryObject<SoundEvent> CYCLE_OFFHAND = SOUND_EVENTS.register(
            "cycle_offhand",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_offhand"))
    );
    public static final RegistryObject<SoundEvent> CYCLE_CONSUMABLE = SOUND_EVENTS.register(
            "cycle_consumable",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_consumable"))
    );
    public static final RegistryObject<SoundEvent> CYCLE_SPELL = SOUND_EVENTS.register(
            "cycle_spell",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "cycle_spell"))
    );
}
