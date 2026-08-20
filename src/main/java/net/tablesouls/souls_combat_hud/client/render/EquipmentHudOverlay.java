package net.tablesouls.souls_combat_hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.client.util.TextAnchor;
import net.tablesouls.souls_combat_hud.client.util.TextHelper;
import net.tablesouls.souls_combat_hud.client.util.animation.FadeAnimator;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.compat.moreoffhandslots.MoreOffhandSlotsCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsProvider;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.client.util.ElementOrientation;
import net.tablesouls.souls_combat_hud.client.util.slots.PreviewRowLayout;
import net.tablesouls.souls_combat_hud.client.util.slots.ConsumableSlotManager;
import net.tablesouls.souls_combat_hud.client.util.slots.WeaponSlotManager;
import org.joml.Vector3f;

public class EquipmentHudOverlay implements IGuiOverlay {

    private static final ResourceLocation SLOT_ATLAS = ResourceLocation.fromNamespaceAndPath("souls_combat_hud", "textures/gui/equipment_slots.png");
    private static final int ATLAS_W = 128;
    private static final int ATLAS_H = 32;

    private static final int SLOT_HALF_W = 12;
    private static final int SLOT_HALF_H = 16;

    private static final int PREVIEW_SIZE = 14;
    private static final int PREVIEW_ITEM_SIZE = 10;
    private static final int PREVIEW_GAP = 4;

    private static final int MAIN_SLOT_W = 24;
    private static final int MAIN_SLOT_H = 32;
    private static final int MAIN_U_WEAPON = 0;
    private static final int MAIN_U_OFFHAND = 24;
    private static final int MAIN_U_CONSUMABLE = 48;
    private static final int MAIN_U_SPELL = 72;
    private static final int MAIN_V = 0;

    private static final int PREVIEW_U_SPELL = 96;
    private static final int PREVIEW_U_CONSUMABLE = 110;
    private static final int PREVIEW_V = 0;

    private static final int ICON_SIZE = 16;
    private static final float WEAPON_PREVIEW_ALPHA = 0.5f;

    private static final int TEXT_COLOR = -1;
    private static final int COOLDOWN_COLOR = Integer.MAX_VALUE;
    private static final int TEXT_PADDING = 4;

    private final NameFadeState consumableNameFade = new NameFadeState();
    private final NameFadeState spellNameFade = new NameFadeState();

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!SoulsCombatHUDConfig.EQUIPMENT_HUD.enabled.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        ElementAnchor anchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.anchor.get();
        int offsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.x.get();
        int offsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.y.get();
        int anchorX = anchor.resolveX(screenWidth, offsetX, 0);
        int anchorY = anchor.resolveY(screenHeight, offsetY, 0);
        float scale = SoulsCombatHUDConfig.EQUIPMENT_HUD.scale.get().floatValue();

        int weaponOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.x.get();
        int weaponOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.y.get();

        int offhandOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.x.get();
        int offhandOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.y.get();

        int consumableOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.x.get();
        int consumableOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.y.get();

        int spellOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.x.get();
        int spellOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.y.get();

        int weaponX = weaponOffsetX;
        int weaponY = weaponOffsetY;

        int offhandX = offhandOffsetX;
        int offhandY = offhandOffsetY;

        int consumableX = consumableOffsetX;
        int consumableY = consumableOffsetY;

        int spellX = spellOffsetX;
        int spellY = spellOffsetY;

        int maxConsumablePreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.maxSlots.get();
        int maxSpellPreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.maxSlots.get();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(anchorX, anchorY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.enabled.get()) {
            ItemStack weaponSlotStack;
            boolean isPreviewOnly;
            ItemStack mainHand = player.getMainHandItem();
            boolean isWeapon = WeaponSlotManager.isWeapon(mainHand);
            if (isWeapon) {
                weaponSlotStack = mainHand;
                isPreviewOnly = false;
                WeaponSlotManager.setLastWeaponSlot(player.getInventory().selected);
            } else {
                int jumpSlot = WeaponSlotManager.getJumpTargetSlot(player);
                weaponSlotStack = jumpSlot >= 0 ? player.getInventory().items.get(jumpSlot) : ItemStack.EMPTY;
                isPreviewOnly = true;
            }
            this.renderWeaponSlot(guiGraphics, mc, weaponX, weaponY, weaponSlotStack, isPreviewOnly);

            if (WeaponSlotManager.hasMultipleWeapons(player)) {
                ElementAnchor previewAnchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.previewSlots.anchor.get();
                int previewX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.previewSlots.x.get();
                int previewY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.previewSlots.y.get();
                int maxWeaponPreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.previewSlots.maxSlots.get();

                List<ItemStack> weaponPreviews = WeaponSlotManager.getPreviews(player, maxWeaponPreviewSlots);
                PreviewRowLayout.render(guiGraphics, weaponX + previewX, weaponY + previewY, SLOT_HALF_W, SLOT_HALF_H,
                        weaponPreviews, maxWeaponPreviewSlots, PREVIEW_SIZE, PREVIEW_GAP, previewAnchor,
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.previewSlots.orientation.get(),
                        (gg, stack, cx, cy) -> this.renderPreviewItemSlot(gg, mc, cx, cy, stack));
            }
        }

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.enabled.get()) {
            this.renderItemSlot(guiGraphics, mc, offhandX, offhandY, player.getOffhandItem(), MAIN_U_OFFHAND);

            if (MoreOffhandSlotsCompat.hasMultipleOffhandItems(player)) {
                ElementAnchor previewAnchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.previewSlots.anchor.get();
                int previewX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.previewSlots.x.get();
                int previewY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.previewSlots.y.get();
                int maxOffhandPreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.previewSlots.maxSlots.get();

                List<ItemStack> offhandPreviews = MoreOffhandSlotsCompat.getOffhandPreviews(player, maxOffhandPreviewSlots);
                PreviewRowLayout.render(guiGraphics, offhandX + previewX, offhandY + previewY, SLOT_HALF_W, SLOT_HALF_H,
                        offhandPreviews, maxOffhandPreviewSlots, PREVIEW_SIZE, PREVIEW_GAP, previewAnchor,
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.previewSlots.orientation.get(),
                        (gg, stack, cx, cy) -> this.renderPreviewItemSlot(gg, mc, cx, cy, stack));
            }
        }

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.enabled.get()) {
            ItemStack consumable = ConsumableSlotManager.getSelected(player);
            this.renderItemSlot(guiGraphics, mc, consumableX, consumableY, consumable, MAIN_U_CONSUMABLE);

            if (!consumable.isEmpty()) {
                String name = consumable.getHoverName().getString();
                int nameY = consumableY + SLOT_HALF_H + TEXT_PADDING;
                this.drawAutoHideName(
                        guiGraphics, mc, consumableNameFade,
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.itemName.enabled.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.itemName.autoHide.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.itemName.holdMillis.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.itemName.fadeMillis.get(),
                        name, consumableX, nameY, anchor.isRight()
                );
            } else if (consumableNameFade.isFullyHidden()) {
                consumableNameFade.reset();
            }

            if (ConsumableSlotManager.hasMultipleConsumables(player)) {
                ElementAnchor previewAnchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.anchor.get();
                ElementOrientation previewOrientation = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.orientation.get();
                int previewX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.x.get();
                int previewY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.y.get();

                List<ItemStack> consumablePreviews = ConsumableSlotManager.getPreviews(player, maxConsumablePreviewSlots);
                PreviewRowLayout.render(
                        guiGraphics,
                        consumableX + previewX,
                        consumableY + previewY,
                        SLOT_HALF_W,
                        SLOT_HALF_H,
                        consumablePreviews,
                        maxConsumablePreviewSlots,
                        PREVIEW_SIZE,
                        PREVIEW_GAP,
                        previewAnchor,
                        previewOrientation,
                        (gg, stack, cx, cy) -> this.renderPreviewItemSlot(gg, mc, cx, cy, stack));
            }
        }

        boolean showSpellSlot = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.enabled.get() && IronsSpellsCompat.LOADED;
        if (showSpellSlot) {
            ResourceLocation spellIcon = IronsSpellsProvider.getSelectedSpellIcon();
            float spellCooldown = IronsSpellsProvider.getSelectedSpellCooldownPercent();
            this.renderIconSlot(guiGraphics, spellX, spellY, spellIcon, spellCooldown);

            String spellName = IronsSpellsProvider.getSelectedSpellName();
            if (spellName != null) {
                int textHeight = 9;
                int nameY = spellY - SLOT_HALF_H - TEXT_PADDING - textHeight;
                this.drawAutoHideName(
                        guiGraphics, mc, spellNameFade,
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.itemName.enabled.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.itemName.autoHide.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.itemName.holdMillis.get(),
                        SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.itemName.fadeMillis.get(),
                        spellName, spellX, nameY, anchor.isRight()
                );
            } else if (spellNameFade.isFullyHidden()) {
                spellNameFade.reset();
            }

            if (IronsSpellsProvider.hasMultipleSpells()) {
                ElementAnchor previewAnchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.anchor.get();
                ElementOrientation previewOrientation = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.orientation.get();
                int previewX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.x.get();
                int previewY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.y.get();
                List<IronsSpellsProvider.SpellPreviewEntry> spellPreviews =
                        IronsSpellsProvider.getPreviewSpellEntries(maxSpellPreviewSlots);
                PreviewRowLayout.render(
                        guiGraphics,
                        spellX + previewX,
                        spellY + previewY,
                        SLOT_HALF_W,
                        SLOT_HALF_H,
                        spellPreviews,
                        maxSpellPreviewSlots,
                        PREVIEW_SIZE,
                        PREVIEW_GAP,
                        previewAnchor,
                        previewOrientation,
                        (gg, entry, cx, cy) -> this.renderPreviewIconSlot(gg, cx, cy, entry.icon(), entry.cooldownPercent()));
            }
        }

        guiGraphics.pose().popPose();
    }

    private void drawAutoHideName(
            GuiGraphics guiGraphics, Minecraft mc, NameFadeState state,
            boolean enabled, boolean autoHide, int holdMillis, int fadeMillis,
            String name, int x, int y, boolean rightAlign
    ) {
        if (!enabled) {
            state.reset();
            return;
        }

        float alpha = state.update(name, autoHide, holdMillis, fadeMillis);
        if (alpha > 0.0f) {
            this.drawName(guiGraphics, mc, x, name, y, rightAlign, alpha);
        }
    }

    private void drawName(GuiGraphics guiGraphics, Minecraft mc, int x, String text, int y, boolean rightAlign, float alpha) {
        TextAnchor anchor = rightAlign ? TextAnchor.INSIDE_RIGHT : TextAnchor.INSIDE_LEFT;
        int textX = TextHelper.resolveTextBaseX(anchor, x - SLOT_HALF_W, SLOT_HALF_W * 2, mc.font.width(text));
        int alphaBits = Mth.clamp(Math.round(alpha * 255f), 0, 255) << 24;
        int color = alphaBits | (TEXT_COLOR & 0x00FFFFFF);
        guiGraphics.drawString(mc.font, text, textX, y, color, true);
    }

    private void renderWeaponSlot(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, ItemStack stack, boolean isPreviewOnly) {
        boolean empty = stack.isEmpty();
        this.blitSlotFrame(guiGraphics, centerX, centerY, MAIN_U_WEAPON, MAIN_V, MAIN_SLOT_W, MAIN_SLOT_H);
        if (!empty) {
            float alpha = isPreviewOnly ? WEAPON_PREVIEW_ALPHA : 1.0f;
            this.renderItemIcon(guiGraphics, mc, centerX, centerY, stack, ICON_SIZE, alpha);
        }
    }

    private void renderItemSlot(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, ItemStack stack, int frameU) {
        boolean empty = stack.isEmpty();
        this.blitSlotFrame(guiGraphics, centerX, centerY, frameU, MAIN_V, MAIN_SLOT_W, MAIN_SLOT_H);
        if (!empty) {
            this.renderItemIcon(guiGraphics, mc, centerX, centerY, stack, ICON_SIZE, 1.0f);
        }
    }

    private void renderPreviewItemSlot(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, ItemStack stack) {
        boolean empty = stack.isEmpty();
        this.blitSlotFrame(guiGraphics, centerX, centerY, PREVIEW_U_CONSUMABLE, PREVIEW_V, PREVIEW_SIZE, PREVIEW_SIZE);
        if (!empty) {
            this.renderItemIcon(guiGraphics, mc, centerX, centerY, stack, PREVIEW_ITEM_SIZE, 1.0f);
        }
    }

    private void renderIconSlot(GuiGraphics guiGraphics, int centerX, int centerY, ResourceLocation icon, float cooldownPercent) {
        boolean empty = icon == null;
        this.blitSlotFrame(guiGraphics, centerX, centerY, MAIN_U_SPELL, MAIN_V, MAIN_SLOT_W, MAIN_SLOT_H);
        if (!empty) {
            this.blitIcon(guiGraphics, icon, centerX, centerY, ICON_SIZE);
            this.renderCooldownOverlay(guiGraphics, centerX, centerY, ICON_SIZE, cooldownPercent);
        }
    }

    private void renderPreviewIconSlot(GuiGraphics guiGraphics, int centerX, int centerY, ResourceLocation icon, float cooldownPercent) {
        boolean empty = icon == null;
        this.blitSlotFrame(guiGraphics, centerX, centerY, PREVIEW_U_SPELL, PREVIEW_V, PREVIEW_SIZE, PREVIEW_SIZE);
        if (!empty) {
            this.blitIcon(guiGraphics, icon, centerX, centerY, PREVIEW_ITEM_SIZE);
            this.renderCooldownOverlay(guiGraphics, centerX, centerY, PREVIEW_SIZE, cooldownPercent);
        }
    }

    private void renderItemIcon(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, ItemStack stack, int iconSize, float alpha) {
        int half = iconSize / 2;
        Vector3f screenCenter = guiGraphics.pose().last().pose().transformPosition(new Vector3f(centerX, centerY, 0));
        float ambientScale = guiGraphics.pose().last().pose().m00();
        int scaledHalf = Math.round(half * ambientScale);
        int scissorMinX = Math.round(screenCenter.x()) - scaledHalf;
        int scissorMinY = Math.round(screenCenter.y()) - scaledHalf;
        int scissorMaxX = Math.round(screenCenter.x()) + scaledHalf;
        int scissorMaxY = Math.round(screenCenter.y()) + scaledHalf;

        float scale = (float) iconSize / 16.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - iconSize / 2.0f, centerY - iconSize / 2.0f, 0.0f);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(mc.font, stack, 0, 0);
        guiGraphics.flush();

        guiGraphics.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);
        RenderSystem.clearDepth(1.0D);
        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, false);
        guiGraphics.disableScissor();

        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void blitIcon(GuiGraphics guiGraphics, ResourceLocation icon, int centerX, int centerY, int size) {
        int x = centerX - size / 2;
        int y = centerY - size / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(icon, x, y, size, size, 0.0f, 0.0f, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    private void renderCooldownOverlay(GuiGraphics guiGraphics, int centerX, int centerY, int size, float cooldownPercent) {
        if (cooldownPercent <= 0.0f) {
            return;
        }
        int x = centerX - size / 2;
        int y = centerY - size / 2;
        int height = Math.round(cooldownPercent * size);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.fill(x, y + size - height, x + size, y + size, COOLDOWN_COLOR);
        RenderSystem.disableBlend();
    }

    private void blitSlotFrame(GuiGraphics guiGraphics, int centerX, int centerY, int u, int v, int regionW, int regionH) {
        int x = centerX - regionW / 2;
        int y = centerY - regionH / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(SLOT_ATLAS, x, y, regionW, regionH, u, v, regionW, regionH, ATLAS_W, ATLAS_H);
        RenderSystem.disableBlend();
    }

    private static final class NameFadeState {
        private FadeAnimator fade;
        private String committedValue;
        private long visibleUntilMillis = 0L;

        float update(String currentValue, boolean autoHide, int holdMillis, int fadeMillis) {
            if (!autoHide) {
                committedValue = currentValue;
                return 1.0f;
            }
            if (fade == null) {
                fade = new FadeAnimator(0L, fadeMillis, 0L, FadeAnimator.Mode.FADE_OUT);
            }

            long now = System.currentTimeMillis();

            if (!Objects.equals(currentValue, committedValue)) {
                committedValue = currentValue;
                visibleUntilMillis = now + holdMillis;
            }

            boolean visible = committedValue != null && now < visibleUntilMillis;
            fade.setVisible(visible);
            return fade.tick();
        }

        void reset() {
            committedValue = null;
            visibleUntilMillis = 0L;
        }

        boolean isFullyHidden() {
            return fade == null || fade.isHidden();
        }
    }
}