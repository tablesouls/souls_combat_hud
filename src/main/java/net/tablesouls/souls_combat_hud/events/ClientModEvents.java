package net.tablesouls.souls_combat_hud.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.BossBarOverlay;
import net.tablesouls.souls_combat_hud.client.EquipmentHudOverlay;
import net.tablesouls.souls_combat_hud.client.SkillOverlay;
import net.tablesouls.souls_combat_hud.registry.ModKeyBindings;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "equipment_hud", new EquipmentHudOverlay());
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "skill_overlay", new SkillOverlay());
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "souls_bossbar", new BossBarOverlay());
    }
}
