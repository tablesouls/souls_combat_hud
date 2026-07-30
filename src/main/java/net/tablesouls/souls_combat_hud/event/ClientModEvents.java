package net.tablesouls.souls_combat_hud.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.boss_bar.BossBarStyleReloadListener;
import net.tablesouls.souls_combat_hud.client.render.bars.boss_bar.BossbarOverlay;
import net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar.OxygenBarOverlay;
import net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar.OxygenBarStyleReloadListener;
import net.tablesouls.souls_combat_hud.client.render.*;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.party.PartyGaugeOverlay;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeStyleReloadListener;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeStyleRegistry;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.GaugeOverlay;
import net.tablesouls.souls_combat_hud.registry.ModKeyBindings;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new BossBarStyleReloadListener());
        event.registerReloadListener(new OxygenBarStyleReloadListener());
        event.registerReloadListener(new GaugeStyleReloadListener("player_gauge.json", GaugeStyleRegistry.PLAYER));
        event.registerReloadListener(new GaugeStyleReloadListener("party_gauge.json", GaugeStyleRegistry.PARTY));
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "equipment_hud", new EquipmentHudOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "skill_overlay", new SkillOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "souls_bossbar", new BossbarOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "oxygen_bar", new OxygenBarOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "experience_overlay", new ExperienceOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "player_gauge_overlay", new GaugeOverlay());
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "party_gauge_overlay", new PartyGaugeOverlay());
    }
}