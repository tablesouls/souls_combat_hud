package net.tablesouls.souls_combat_hud.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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

@EventBusSubscriber(modid = SoulsCombatHUD.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    public static final BossBarStyleReloadListener BOSS_BAR_STYLE_LISTENER = new BossBarStyleReloadListener();
    public static final OxygenBarStyleReloadListener OXYGEN_BAR_STYLE_LISTENER = new OxygenBarStyleReloadListener();
    public static final GaugeStyleReloadListener PLAYER_GAUGE_STYLE_LISTENER =
            new GaugeStyleReloadListener("player_gauge.json", GaugeStyleRegistry.PLAYER);
    public static final GaugeStyleReloadListener PARTY_GAUGE_STYLE_LISTENER =
            new GaugeStyleReloadListener("party_gauge.json", GaugeStyleRegistry.PARTY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(BOSS_BAR_STYLE_LISTENER);
        event.registerReloadListener(OXYGEN_BAR_STYLE_LISTENER);
        event.registerReloadListener(PLAYER_GAUGE_STYLE_LISTENER);
        event.registerReloadListener(PARTY_GAUGE_STYLE_LISTENER);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "equipment_hud"), new EquipmentHudOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "skill_overlay"), new SkillOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "souls_bossbar"), new BossbarOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "oxygen_bar"), new OxygenBarOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "experience_overlay"), new ExperienceOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "player_gauge_overlay"), new GaugeOverlay());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "party_gauge_overlay"), new PartyGaugeOverlay());
    }
}