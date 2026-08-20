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
import net.tablesouls.souls_combat_hud.compat.helditemtooltips.HeldItemTooltipsCompat;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

@Mod.EventBusSubscriber(modid = "souls_combat_hud", bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ClientForgeRenderEvents {
    private static final ResourceLocation WEAPON_INNATE_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "weapon_innate");
    private static final ResourceLocation SKILLS_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "skills");
    private static final ResourceLocation STAMINA_BAR_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "stamina_bar");
    private static final ResourceLocation OFFHAND_HUD_ID = ResourceLocation.fromNamespaceAndPath(MoreOffhandSlots.MODID, "offhand_hud");
    private static final ResourceLocation TWR_THIRST_OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(ThirstWasReclaimedCompat.MODID, "thirst_level");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemNameOffsetPre(RenderGuiOverlayEvent.Pre event) {
        if (!HeldItemTooltipsCompat.LOADED) return;
        applyOverlayOffset(event);
    }

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

        var statusBarSetting = getStatusBarSetting(id);
        if (statusBarSetting != null) {
            if (statusHidden() || statusBarSetting.hidden.get()) {
                event.setCanceled(true);
            } else if (!HeldItemTooltipsCompat.LOADED) {
                applyStatusOverlayOffset(id, event);
            }
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

        if (!HeldItemTooltipsCompat.LOADED) {
            applyOverlayOffset(event);
        }
    }

    private static boolean statusHidden() {
        return SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.hidden.get();
    }

    private static void applyOverlayOffset(RenderGuiOverlayEvent.Pre event) {
        ResourceLocation id = event.getOverlay().id();

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

        if (isStatusBarOverlay(id)) {
            applyStatusOverlayOffset(id, event);
        }
    }

    private static boolean isStatusBarOverlay(ResourceLocation id) {
        return getStatusBarSetting(id) != null;
    }

    /**
     * Maps a vanilla status-bar overlay id to its corresponding config setting,
     * or returns null if the id isn't one of the status bar overlays.
     */
    private static SoulsCombatHUDConfig.Visibility.MinecraftGuiSetting.MinecraftHotbarSetting.MinecraftHotbarStatusSetting.MinecraftHotbarStatusBarSetting getStatusBarSetting(ResourceLocation id) {
        var status = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status;

        if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            return status.healthLevel;
        } else if (id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            return status.armorLevel;
        } else if (id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            return status.foodLevel;
        } else if (id.equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            return status.airLevel;
        } else if (id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            return status.xpBar;
        }
        return null;
    }

    private static void applyStatusOverlayOffset(ResourceLocation id, RenderGuiOverlayEvent.Pre event) {
        int x = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.x.get();
        int y = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.y.get();

        if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.healthLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.healthLevel.y.get();
        } else if (id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.armorLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.armorLevel.y.get();
        } else if (id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.foodLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.foodLevel.y.get();
        } else if (id.equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.airLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.airLevel.y.get();
        } else if (id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.xpBar.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.xpBar.y.get();
        }

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(x, -y, 0);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onGuiOffsetPop(RenderGuiOverlayEvent.Pre event) {
        if (!HeldItemTooltipsCompat.LOADED) return;
        if (!event.isCanceled()) return;

        ResourceLocation id = event.getOverlay().id();
        if (id.equals(VanillaGuiOverlay.ITEM_NAME.id())
                || id.equals(VanillaGuiOverlay.RECORD_OVERLAY.id())
                || isStatusBarOverlay(id)
        ) {
            event.getGuiGraphics().pose().popPose();
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
                || isStatusBarOverlay(id)
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