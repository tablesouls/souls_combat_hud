package net.tablesouls.souls_combat_hud.event;

import akkynaa.moreoffhandslots.MoreOffhandSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.client.util.BossBarState;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

@Mod.EventBusSubscriber(modid = "souls_combat_hud", bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ClientForgeRenderEvents {
    private static final ResourceLocation WEAPON_INNATE_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "weapon_innate");
    private static final ResourceLocation SKILLS_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "skills");
    private static final ResourceLocation STAMINA_BAR_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "stamina_bar");
    private static final ResourceLocation OFFHAND_HUD_ID = ResourceLocation.fromNamespaceAndPath(MoreOffhandSlots.MODID, "offhand_hud");
    private static final ResourceLocation TWR_THIRST_OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(ThirstWasReclaimedCompat.MODID, "thirst_level");

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        ResourceLocation id = event.getOverlay().id();

        if (id.equals(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) {
            BossBarState.beginFrame();
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hidePotionIcons.get()
                && id.equals(VanillaGuiOverlay.POTION_ICONS.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideHealthLevel.get()
                && id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideArmorLevel.get()
                && id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideHungerLevel.get()
                && id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideAirLevel.get()
                && id.equals(VanillaGuiOverlay.AIR_LEVEL.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideExperienceBar.get()
                && id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get()
                && (id.equals(WEAPON_INNATE_ID) || id.equals(SKILLS_ID))
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.epicfightGui.hideStaminaBar.get()
                && (id.equals(STAMINA_BAR_ID))
        ) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.hideMoreOffhandSlots.get() && id.equals(OFFHAND_HUD_ID)) {
            event.setCanceled(true);
            return;
        }

        if (SoulsCombatHUDConfig.VISIBILITY.thirstGui.hideThirst.get() && id.equals(TWR_THIRST_OVERLAY_ID)) {
            event.setCanceled(true);
            return;
        }

        if (id.equals(VanillaGuiOverlay.ITEM_NAME.id())) {
            int y = getHudYOffset() - SoulsCombatHUDConfig.VISIBILITY.minecraftGui.itemName.y.get();

            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(
                    SoulsCombatHUDConfig.VISIBILITY.minecraftGui.itemName.x.get(),
                    y,
                    0
            );
        }

        if (id.equals(VanillaGuiOverlay.RECORD_OVERLAY.id())) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(
                    SoulsCombatHUDConfig.VISIBILITY.minecraftGui.recordOverlay.x.get(),
                    -SoulsCombatHUDConfig.VISIBILITY.minecraftGui.recordOverlay.y.get(),
                    0
            );
        }
    }

    private static int getHudYOffset() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gameMode != null
                && mc.gameMode.getPlayerMode() == GameType.CREATIVE
                && SoulsCombatHUDConfig.VISIBILITY.minecraftGui.disableShiftTextOnGamemode.get()
                ? -14 : 0;
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayEventPost (RenderGuiOverlayEvent.Post event) {
        if (event.isCanceled()) return;
        ResourceLocation id = event.getOverlay().id();

        if (id.equals(VanillaGuiOverlay.ITEM_NAME.id())
            || id.equals(VanillaGuiOverlay.RECORD_OVERLAY.id())
        ) {
            event.getGuiGraphics().pose().popPose();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (SoulsCombatHUDConfig.CUSTOM_BOSSBAR.enabled.get()) {
            BossBarState.update(event.getBossEvent().getId(), event.getBossEvent().getName(), event.getBossEvent().getProgress());
        }
        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hideBossbar.get()) {
            event.setCanceled(true);
        }
    }
}