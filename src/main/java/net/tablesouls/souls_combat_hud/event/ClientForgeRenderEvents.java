package net.tablesouls.souls_combat_hud.event;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tablesouls.souls_combat_hud.client.util.BossBarState;
import net.tablesouls.souls_combat_hud.compat.ars_nouveau.ArsNouveauCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.helditemtooltips.HeldItemTooltipsCompat;
import net.tablesouls.souls_combat_hud.compat.moreoffhandslots.MoreOffhandSlotsCompat;
import net.tablesouls.souls_combat_hud.compat.thirst_was_reclaimed.ThirstWasReclaimedCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;

@EventBusSubscriber(modid = "souls_combat_hud", value = {Dist.CLIENT})
public class ClientForgeRenderEvents {
    private static final ResourceLocation WEAPON_INNATE_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "weapon_innate");
    private static final ResourceLocation STAMINA_BAR_ID = ResourceLocation.fromNamespaceAndPath(EpicFightCompat.MODID, "stamina_bar");
    private static final ResourceLocation OFFHAND_HUD_ID = ResourceLocation.fromNamespaceAndPath(MoreOffhandSlotsCompat.MODID, "offhand_hud");
    private static final ResourceLocation TWR_THIRST_OVERLAY_ID = ResourceLocation.fromNamespaceAndPath(ThirstWasReclaimedCompat.MODID, "thirst_level");
    private static final ResourceLocation ARS_NOUVEAU_MANA_HUD_ID = ResourceLocation.fromNamespaceAndPath(ArsNouveauCompat.MODID, "mana_hud");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemNameOffsetPre(RenderGuiLayerEvent.Pre event) {
        if (!HeldItemTooltipsCompat.LOADED) return;
        applyOverlayOffset(event);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiLayerEvent.Pre event) {
        ResourceLocation id = event.getName();

        if (id.equals(VanillaGuiLayers.BOSS_OVERLAY)) {
            BossBarState.beginFrame();
        }

        if (SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hidePotionIcons.get()
                && id.equals(VanillaGuiLayers.EFFECTS)
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

        if (SoulsCombatHUDConfig.SKILL_OVERLAY.enabled.get() && id.equals(WEAPON_INNATE_ID)) {
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

        if (SoulsCombatHUDConfig.VISIBILITY.arsNouveauGui.hideManaBar.get() && id.equals(ARS_NOUVEAU_MANA_HUD_ID)) {
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

    private static void applyOverlayOffset(RenderGuiLayerEvent.Pre event) {
        ResourceLocation id = event.getName();

        if (id.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
            int y = getHudYOffset() - SoulsCombatHUDConfig.VISIBILITY.minecraftGui.itemName.y.get();

            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(
                    SoulsCombatHUDConfig.VISIBILITY.minecraftGui.itemName.x.get(),
                    y,
                    0
            );
        }

        if (id.equals(VanillaGuiLayers.OVERLAY_MESSAGE)) {
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

    private static SoulsCombatHUDConfig.Visibility.MinecraftGuiSetting.MinecraftHotbarSetting.MinecraftHotbarStatusSetting.MinecraftHotbarStatusBarSetting getStatusBarSetting(ResourceLocation id) {
        var status = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status;

        if (id.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            return status.healthLevel;
        } else if (id.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            return status.armorLevel;
        } else if (id.equals(VanillaGuiLayers.FOOD_LEVEL)) {
            return status.foodLevel;
        } else if (id.equals(VanillaGuiLayers.AIR_LEVEL)) {
            return status.airLevel;
        } else if (id.equals(VanillaGuiLayers.EXPERIENCE_BAR)) {
            return status.xpBar;
        }
        return null;
    }

    private static void applyStatusOverlayOffset(ResourceLocation id, RenderGuiLayerEvent.Pre event) {
        int x = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.x.get();
        int y = SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.y.get();

        if (id.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.healthLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.healthLevel.y.get();
        } else if (id.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.armorLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.armorLevel.y.get();
        } else if (id.equals(VanillaGuiLayers.FOOD_LEVEL)) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.foodLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.foodLevel.y.get();
        } else if (id.equals(VanillaGuiLayers.AIR_LEVEL)) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.airLevel.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.airLevel.y.get();
        } else if (id.equals(VanillaGuiLayers.EXPERIENCE_BAR)) {
            x += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.xpBar.x.get();
            y += SoulsCombatHUDConfig.VISIBILITY.minecraftGui.hotbar.status.xpBar.y.get();
        }

        event.getGuiGraphics().pose().pushPose();
        event.getGuiGraphics().pose().translate(x, -y, 0);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onGuiOffsetPop(RenderGuiLayerEvent.Pre event) {
        if (!HeldItemTooltipsCompat.LOADED) return;
        if (!event.isCanceled()) return;

        ResourceLocation id = event.getName();
        if (id.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || id.equals(VanillaGuiLayers.OVERLAY_MESSAGE)
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
    public static void onRenderGuiOverlayEventPost (RenderGuiLayerEvent.Post event) {
        ResourceLocation id = event.getName();

        if (id.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)
                || id.equals(VanillaGuiLayers.OVERLAY_MESSAGE)
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