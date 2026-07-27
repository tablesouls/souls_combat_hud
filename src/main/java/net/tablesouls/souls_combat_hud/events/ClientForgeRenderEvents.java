package net.tablesouls.souls_combat_hud.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.util.BossBarState;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

@Mod.EventBusSubscriber(modid = "souls_combat_hud", bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ClientForgeRenderEvents {
    private static final ResourceLocation WEAPON_INNATE_ID = ResourceLocation.fromNamespaceAndPath("epicfight", "weapon_innate");
    private static final ResourceLocation SKILLS_ID = ResourceLocation.fromNamespaceAndPath("epicfight", "skills");
    private static final ResourceLocation OFFHAND_HUD_ID = ResourceLocation.fromNamespaceAndPath("moreoffhandslots", "offhand_hud");

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        ResourceLocation id = event.getOverlay().id();

        if (id.equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) {
            BossBarState.beginFrame();
        }

        if (SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get() && (id.equals(WEAPON_INNATE_ID) || id.equals(SKILLS_ID))) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.hideMoreOffhandSlots.get() && id.equals(OFFHAND_HUD_ID)) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (SoulsCombatHUDConfig.CUSTOM_BOSSBAR.enabled.get()) {
            BossBarState.update(event.getBossEvent().getId(), event.getBossEvent().getName(), event.getBossEvent().getProgress());
        }
        if (SoulsCombatHUDConfig.VISIBILITY.hideBossbar.get()) {
            event.setCanceled(true);
        }
    }
}