package net.tablesouls.souls_combat_hud.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

import java.util.HashMap;
import java.util.Map;

public class SoundHelper {
    private static final Map<ResourceLocation, SoundEvent> ITEM_SOUNDS = new HashMap<>();

    public static void register(ResourceLocation itemid, SoundEvent sound) {
        ITEM_SOUNDS.put(itemid, sound);
    }

    public static void playUiSound(SoundEvent sound, float volume, float pitch) {
        if (sound == null) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    public static void playUiSound(SoundEvent sound) {
        playUiSound(sound, 1.0f, 1.0f);
    }

    public static void playCycleSound(SoundEvent sound) {
        if (!SoulsCombatHUDConfig.EQUIPMENT_HUD.cycleSound.get()) return;
        playUiSound(sound);
    }
}