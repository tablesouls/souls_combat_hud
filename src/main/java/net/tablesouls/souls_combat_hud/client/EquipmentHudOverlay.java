package net.tablesouls.souls_combat_hud.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksCompat;
import net.tablesouls.souls_combat_hud.compat.moreoffhandslots.MoreOffhandSlotsCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksSpellProvider;
import net.tablesouls.souls_combat_hud.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.util.ElementOrientation;
import net.tablesouls.souls_combat_hud.util.slots.PreviewRowLayout;
import net.tablesouls.souls_combat_hud.util.slots.ConsumableSlotManager;
import net.tablesouls.souls_combat_hud.util.slots.WeaponSlotManager;

public class EquipmentHudOverlay implements IGuiOverlay {

    private static final ResourceLocation SLOT_ATLAS = ResourceLocation.fromNamespaceAndPath("souls_combat_hud", "textures/gui/equipment_slots.png");
    private static final int ATLAS_W = 128;
    private static final int ATLAS_H = 32;

    private static final int SLOT_HALF_W = 12;
    private static final int SLOT_HALF_H = 16;

    private static final int PREVIEW_SIZE = 12;
    private static final int PREVIEW_GAP = 4;

    private static final int MAIN_SLOT_W = 24;
    private static final int MAIN_SLOT_H = 32;
    private static final int MAIN_U_WEAPON = 0;
    private static final int MAIN_U_OFFHAND = 24;
    private static final int MAIN_U_CONSUMABLE = 48;
    private static final int MAIN_U_SPELL = 72;
    private static final int MAIN_V = 0;

    private static final int PREVIEW_U_SPELL = 96;
    private static final int PREVIEW_U_CONSUMABLE = 108;
    private static final int PREVIEW_V = 0;

    private static final int ICON_SIZE = 16;
    private static final float WEAPON_PREVIEW_ALPHA = 0.5f;

    private static final int TEXT_COLOR = -1;
    private static final int COOLDOWN_COLOR = Integer.MAX_VALUE;
    private static final int TEXT_PADDING = 4;

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

        int weaponOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.x.get();
        int weaponOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.y.get();

        int offhandOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.x.get();
        int offhandOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.y.get();

        int consumableOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.x.get();
        int consumableOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.y.get();

        int spellOffsetX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.x.get();
        int spellOffsetY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.y.get();

        int weaponX = anchorX + weaponOffsetX;
        int weaponY = anchorY + weaponOffsetY;

        int offhandX = anchorX + offhandOffsetX;
        int offhandY = anchorY + offhandOffsetY;

        int consumableX = anchorX + consumableOffsetX;
        int consumableY = anchorY + consumableOffsetY;

        int spellX = anchorX + spellOffsetX;
        int spellY = anchorY + spellOffsetY;

        int maxConsumablePreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.maxSlots.get();
        int maxSpellPreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.maxSlots.get();

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
                if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.name.enabled.get()) {
                    String name = consumable.getHoverName().getString();
                    int nameY = consumableY + SLOT_HALF_H + TEXT_PADDING;
                    this.drawName(guiGraphics, mc, consumableX, name, nameY, anchor.isRight());
                };
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

        boolean showSpellSlot = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.enabled.get() && IronsSpellbooksCompat.LOADED;
        if (showSpellSlot) {
            ResourceLocation spellIcon = IronsSpellbooksSpellProvider.getSelectedSpellIcon();
            float spellCooldown = IronsSpellbooksSpellProvider.getSelectedSpellCooldownPercent();
            this.renderIconSlot(guiGraphics, spellX, spellY, spellIcon, spellCooldown);

            String spellName = IronsSpellbooksSpellProvider.getSelectedSpellName();
            if (spellName != null) {
                if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.name.enabled.get()) {
                    int textHeight = 9;
                    int nameY = spellY - SLOT_HALF_H - TEXT_PADDING - textHeight;
                    this.drawName(guiGraphics, mc, spellX, spellName, nameY, anchor.isRight());
                }
            }

            if (IronsSpellbooksSpellProvider.hasMultipleSpells()) {
                ElementAnchor previewAnchor = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.anchor.get();
                ElementOrientation previewOrientation = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.previewSlots.orientation.get();
                int previewX = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.x.get();
                int previewY = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.previewSlots.y.get();
                List<IronsSpellbooksSpellProvider.SpellPreviewEntry> spellPreviews =
                        IronsSpellbooksSpellProvider.getPreviewSpellEntries(maxSpellPreviewSlots);
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
    }

    private void drawName(GuiGraphics guiGraphics, Minecraft mc, int x, String text, int y, boolean rightAlign) {
        int textX = rightAlign ? x + SLOT_HALF_W - mc.font.width(text) : x - SLOT_HALF_W;
        guiGraphics.drawString(mc.font, text, textX, y, TEXT_COLOR, true);
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
            this.renderItemIcon(guiGraphics, mc, centerX, centerY, stack, PREVIEW_SIZE, 1.0f);
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
            this.blitIcon(guiGraphics, icon, centerX, centerY, PREVIEW_SIZE);
            this.renderCooldownOverlay(guiGraphics, centerX, centerY, PREVIEW_SIZE, cooldownPercent);
        }
    }

    private void renderItemIcon(GuiGraphics guiGraphics, Minecraft mc, int centerX, int centerY, ItemStack stack, int iconSize, float alpha) {
        float scale = (float) iconSize / 16.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX - iconSize / 2.0f, centerY - iconSize / 2.0f, 0.0f);
        guiGraphics.pose().scale(scale, scale, 1.0f);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(mc.font, stack, 0, 0);
        guiGraphics.flush();

        int half = iconSize/2;
        guiGraphics.enableScissor(centerX - half, centerY - half, centerX + half, centerY + half);
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
}