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
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksSpellProvider;
import net.tablesouls.souls_combat_hud.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.util.PreviewRowLayout;
import net.tablesouls.souls_combat_hud.util.slots.ConsumableSlotManager;
import net.tablesouls.souls_combat_hud.util.slots.WeaponSlotManager;

public class EquipmentHudOverlay implements IGuiOverlay {

    private static final ResourceLocation SLOT_ATLAS = ResourceLocation.fromNamespaceAndPath("souls_combat_hud", "textures/gui/equipment_slots.png");
    private static final int ATLAS_W = 128;
    private static final int ATLAS_H = 32;

    private static final int WEAPON_SLOT_OFFSET = 28;
    private static final int OFFHAND_SLOT_OFFSET = 28;
    private static final int SPELL_SLOT_OFFSET = 18;

    private static final int SLOT_HALF_W = 12;
    private static final int SLOT_HALF_H = 16;

    private static final int PREVIEW_SIZE = 12;
    private static final int PREVIEW_HALF = 6;
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

        int rightX = anchorX + WEAPON_SLOT_OFFSET;
        int leftX = anchorX - OFFHAND_SLOT_OFFSET;
        int bottomY = anchorY + SPELL_SLOT_OFFSET;
        int topY = anchorY - SPELL_SLOT_OFFSET;

        boolean anchoredRight = anchor.isRight();
        boolean growRight = !anchoredRight;
        int previewCenterX = anchoredRight
                ? leftX + SLOT_HALF_W - PREVIEW_HALF
                : rightX - SLOT_HALF_W + PREVIEW_HALF;

        int maxConsumablePreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.maxPreviewSlots.get();
        int maxSpellPreviewSlots = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.maxPreviewSlots.get();

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.weapon.enabled.get()) {
            ItemStack weaponSlotStack;
            boolean isPreviewOnly;
            ItemStack mainHand = player.getMainHandItem();
            boolean isWeapon = WeaponSlotManager.isWeapon(mainHand);
            if (isWeapon) {
                weaponSlotStack = mainHand;
                isPreviewOnly = false;
            } else {
                int jumpSlot = WeaponSlotManager.getJumpTargetSlot(player);
                weaponSlotStack = jumpSlot >= 0 ? player.getInventory().items.get(jumpSlot) : ItemStack.EMPTY;
                isPreviewOnly = true;
            }
            this.renderWeaponSlot(guiGraphics, mc, rightX, anchorY, weaponSlotStack, isPreviewOnly);
        }

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.offhand.enabled.get()) {
            this.renderItemSlot(guiGraphics, mc, leftX, anchorY, player.getOffhandItem(), MAIN_U_OFFHAND);
        }

        if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.enabled.get()) {
            ItemStack consumable = ConsumableSlotManager.getSelected(player);
            this.renderItemSlot(guiGraphics, mc, anchorX, bottomY, consumable, MAIN_U_CONSUMABLE);

            if (!consumable.isEmpty()) {
                if (!SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.name.enabled.get()) return;
                String name = consumable.getHoverName().getString();
                int nameY = bottomY + SLOT_HALF_H + TEXT_PADDING;
                this.drawName(guiGraphics, mc, anchorX, name, nameY, anchor.isRight());
            }

            int bottomPreviewY = bottomY + SLOT_HALF_H - PREVIEW_HALF;
            if (ConsumableSlotManager.hasMultipleConsumables(player)) {
                List<ItemStack> consumablePreviews = ConsumableSlotManager.getPreviews(player, maxConsumablePreviewSlots);
                PreviewRowLayout.render(guiGraphics, previewCenterX, bottomPreviewY, consumablePreviews,
                        maxConsumablePreviewSlots, PREVIEW_SIZE, PREVIEW_GAP, growRight,
                        (gg, stack, cx, cy) -> this.renderPreviewItemSlot(gg, mc, cx, cy, stack));
            }
        }

        boolean showSpellSlot = SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.enabled.get() && IronsSpellbooksCompat.LOADED;
        if (showSpellSlot) {
            ResourceLocation spellIcon = IronsSpellbooksSpellProvider.getSelectedSpellIcon();
            float spellCooldown = IronsSpellbooksSpellProvider.getSelectedSpellCooldownPercent();
            this.renderIconSlot(guiGraphics, anchorX, topY, spellIcon, spellCooldown);

            String spellName = IronsSpellbooksSpellProvider.getSelectedSpellName();
            if (spellName != null) {
                if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.spell.name.enabled.get()) {
                    int textHeight = 9;
                    int nameY = topY - SLOT_HALF_H - TEXT_PADDING - textHeight;
                    this.drawName(guiGraphics, mc, anchorX, spellName, nameY, anchor.isRight());
                };
            }

            int topPreviewY = topY - SLOT_HALF_H + PREVIEW_HALF;
            if (IronsSpellbooksSpellProvider.hasMultipleSpells()) {
                List<IronsSpellbooksSpellProvider.SpellPreviewEntry> spellPreviews =
                        IronsSpellbooksSpellProvider.getPreviewSpellEntries(maxSpellPreviewSlots);
                PreviewRowLayout.render(guiGraphics, previewCenterX, topPreviewY, spellPreviews,
                        maxSpellPreviewSlots, PREVIEW_SIZE, PREVIEW_GAP, growRight,
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