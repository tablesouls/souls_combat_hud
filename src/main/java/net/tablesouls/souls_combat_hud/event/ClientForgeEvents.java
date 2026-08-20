package net.tablesouls.souls_combat_hud.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.gui.PartySortScreen;
import net.tablesouls.souls_combat_hud.client.util.LocalResourceFallback;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.ManaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.StaminaSourceRegistry;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.party.client.PartyMemberProfileCache;
import net.tablesouls.souls_combat_hud.party.network.PartyNetwork;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.util.AutoConsumeHelper;
import net.tablesouls.souls_combat_hud.client.util.slots.ConsumableSlotManager;
import net.tablesouls.souls_combat_hud.registry.ModKeyBindings;
import net.tablesouls.souls_combat_hud.client.util.SoundHelper;
import net.tablesouls.souls_combat_hud.client.util.slots.WeaponSlotManager;
import net.tablesouls.souls_combat_hud.compat.moreoffhandslots.MoreOffhandSlotsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsProvider;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ClientForgeEvents {
    private static boolean autoEating;
    private static boolean autoCastingScroll;
    private static int castingGraceTicks;
    private static ItemStack pendingAutoConsumeStack;
    private static int pendingConsumeDelay;
    private static int previousSlot;
    private static int jumpedToSlot = -1;
    private static boolean pendingRestoreSlot;

    private static void jumpOrCycleWeapon(Player player) {
        if (player.isUsingItem()) return;

        boolean canSwitch = EpicFightCompat.canSwitchHoldingItem(player);
        if (!canSwitch) return;

        int slot = WeaponSlotManager.getNextSlot(player);
        if (slot < 0) return;

        player.getInventory().selected = slot;

        SoundHelper.playCycleSound(ModSounds.CYCLE_WEAPON);

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static void switchToConsumableChain(Player player, int slot) {
        int fromSlot = player.getInventory().selected;

        AutoConsumeHelper.stop();
        if (player.isUsingItem()) {
            Minecraft.getInstance().gameMode.releaseUsingItem(player);
        }
        autoEating = false;

        player.getInventory().selected = slot;

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }

        if (ConsumableSlotManager.isAutoConsumeExcluded(player.getInventory().items.get(slot))) {
            ConsumableSlotManager.setSelectedToHeldItem(player);
            cancelPendingRestore();
            return;
        }

        if (previousSlot < 0) {
            previousSlot = fromSlot;
        }
        jumpedToSlot = slot;
        pendingAutoConsumeStack = player.getMainHandItem();
        pendingConsumeDelay = 2;
    }

    private static void jumpToCycledConsumable(Player player) {
        if (!EpicFightCompat.canSwitchHoldingItem(player)) return;

        int slot = ConsumableSlotManager.getSelectedHotbarSlot(player);
        if (slot < 0 || slot == player.getInventory().selected) {
            return;
        }

        if (autoEating) {
            AutoConsumeHelper.stop();
            if (player.isUsingItem()) {
                Minecraft.getInstance().gameMode.releaseUsingItem(player);
            }
            autoEating = false;
            cancelPendingRestore();
        }

        int fromSlot = player.getInventory().selected;
        player.getInventory().selected = slot;

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }

        if (ConsumableSlotManager.isAutoConsumeExcluded(player.getInventory().items.get(slot))) {
            ConsumableSlotManager.setSelectedToHeldItem(player);
            cancelPendingRestore();
            return;
        }

        previousSlot = fromSlot;
        jumpedToSlot = slot;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        LocalResourceFallback.tick();

        if (ModKeyBindings.CYCLE_WEAPON.consumeClick()) {
            jumpOrCycleWeapon(player);
        }

        while (ModKeyBindings.CYCLE_OFFHAND.consumeClick()) {
            if (!EpicFightCompat.canSwitchHoldingItem(player)) return;
            MoreOffhandSlotsCompat.cycleOffhand(true);
        }

        while (ModKeyBindings.CYCLE_CONSUMABLE.consumeClick()) {
            ConsumableSlotManager.cycle(player);
            if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.cycleConsumableSwitch.get()) {
                jumpToCycledConsumable(player);
            }
        }

        while (ModKeyBindings.CYCLE_SPELL.consumeClick()) {
            IronsSpellsProvider.cycle();
        }

        if (ModKeyBindings.USE_CONSUMABLE.consumeClick()) {
            jumpToSelectedConsumable(player);
        }

        if (ModKeyBindings.OPEN_PARTY_MENU.consumeClick() && Minecraft.getInstance().screen == null) {
            if (PartyNetwork.serverSupportsParty()) {
                Minecraft.getInstance().setScreen(new PartySortScreen());
            } else {
                player.displayClientMessage(
                        Component.translatable("party.souls_combat_hud.unavailable"),
                        true
                );
            }
        }

        if (pendingAutoConsumeStack != null) {
            if (pendingConsumeDelay > 0) {
                pendingConsumeDelay--;
            } else {
                if (player.getInventory().selected == jumpedToSlot
                        && ItemStack.isSameItem(player.getMainHandItem(), pendingAutoConsumeStack)) {
                    beginAutoConsume(player);
                } else {
                    previousSlot = -1;
                    jumpedToSlot = -1;
                }
                pendingAutoConsumeStack = null;
                pendingConsumeDelay = -1;
            }
        }

        if (autoCastingScroll) {
            if (castingGraceTicks > 0) {
                castingGraceTicks--;
            } else if (!IronsSpellsCompat.isCasting()) {
                autoCastingScroll = false;
                restorePreviousSlot(player);
            }
        }

        if (autoEating && !player.isUsingItem()) {
            stopAutoEat(player);
        }

        if (pendingRestoreSlot && !Minecraft.getInstance().options.keyUse.isDown()) {
            restorePreviousSlot(player);
        }
    }

    private static void jumpToSelectedConsumable(Player player) {
        if (!EpicFightCompat.canSwitchHoldingItem(player)) return;

        int slot = ConsumableSlotManager.getSelectedHotbarSlot(player);

        if (autoEating) {
            if (slot < 0 || slot == player.getInventory().selected) {
                cancelAutoEat(player);
                return;
            }
            switchToConsumableChain(player, slot);
            return;
        }

        if (player.isUsingItem()) return;

        ItemStack mainHand = player.getMainHandItem();
        if (ConsumableSlotManager.isConsumable(mainHand, player)) {
            if (SoulsCombatHUDConfig.EQUIPMENT_HUD.slots.consumable.useConsumableOnSelected.get()) {
                ConsumableSlotManager.setSelectedToHeldItem(player);
                if (ConsumableSlotManager.isAutoConsumeExcluded(mainHand)) {
                    return;
                }
                beginAutoConsume(player);
                return;
            }
        }

        if (slot < 0) {
            return;
        }

        ItemStack targetStack = player.getInventory().items.get(slot);
        boolean excluded = ConsumableSlotManager.isAutoConsumeExcluded(targetStack);

        if (player.getInventory().selected != slot) {
            int fromSlot = player.getInventory().selected;
            player.getInventory().selected = slot;

            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.send(new ServerboundSetCarriedItemPacket(slot));
            }

            if (excluded) {
                ConsumableSlotManager.setSelectedToHeldItem(player);
                cancelPendingRestore();
                return;
            }

            previousSlot = fromSlot;
            jumpedToSlot = slot;
            pendingAutoConsumeStack = player.getMainHandItem();
            pendingConsumeDelay = 2;
            return;
        }

        if (excluded) {
            return;
        }

        if (jumpedToSlot != slot) {
            previousSlot = -1;
            jumpedToSlot = -1;
        }
        beginAutoConsume(player);
    }

    private static void beginAutoConsume(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!ConsumableSlotManager.isConsumable(stack, player)
                && !IronsSpellsCompat.isScroll(stack)) return;

        Minecraft.getInstance().gameMode.useItem(player, InteractionHand.MAIN_HAND);

        if (IronsSpellsCompat.isScroll(stack)) {
            autoCastingScroll = true;
            castingGraceTicks = 3;
            return;
        }

        if (player.isUsingItem()) {
            autoEating = true;
            AutoConsumeHelper.start(stack.getItem());
            return;
        }

        cancelPendingRestore();
    }

    private static void cancelPendingRestore() {
        previousSlot = -1;
        jumpedToSlot = -1;
        pendingRestoreSlot = false;
    }

    private static void restorePreviousSlot(Player player) {
        if (jumpedToSlot < 0) {
            return;
        }

        // If the player manually switched away from the consumable slot, dont return
        if (player.getInventory().selected != jumpedToSlot) {
            previousSlot = -1;
            jumpedToSlot = -1;
            pendingRestoreSlot = false;
            return;
        }

        // Never write an invalid slot back into the hotbar
        if (previousSlot < 0) {
            jumpedToSlot = -1;
            pendingRestoreSlot = false;
            return;
        }

        if (Minecraft.getInstance().options.keyUse.isDown()) {
            pendingRestoreSlot = true;
            return;
        }

        pendingRestoreSlot = false;
        int slotToRestore = previousSlot;
        previousSlot = -1;
        jumpedToSlot = -1;

        player.getInventory().selected = slotToRestore;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slotToRestore));
        }
    }

    private static void cancelAutoEat(Player player) {
        if (!autoEating) {
            return;
        }

        AutoConsumeHelper.stop();

        if (player.isUsingItem()) {
            Minecraft.getInstance().gameMode.releaseUsingItem(player);
        }

        autoEating = false;
        restorePreviousSlot(player);
    }

    private static void stopAutoEat(Player player) {
        autoEating = false;
        AutoConsumeHelper.stop();
        restorePreviousSlot(player);
    }

    private static void resetState() {
        autoEating = false;
        autoCastingScroll = false;
        castingGraceTicks = 0;
        pendingAutoConsumeStack = null;
        pendingConsumeDelay = -1;
        previousSlot = -1;
        jumpedToSlot = -1;
        pendingRestoreSlot = false;
        AutoConsumeHelper.stop();
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        PartyNetwork.resetServerSupportsParty();
        TeamProviderRegistry.reset();
        ManaSourceRegistry.INSTANCE.resetActiveMode();
        StaminaSourceRegistry.INSTANCE.resetActiveMode();
        LocalResourceFallback.reset();
        resetState();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PartyNetwork.resetServerSupportsParty();
        TeamProviderRegistry.reset();
        ManaSourceRegistry.INSTANCE.resetActiveMode();
        StaminaSourceRegistry.INSTANCE.resetActiveMode();
        PartyMemberProfileCache.flush();
        resetState();
    }

    @SubscribeEvent
    public static void onStopUsingItem(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() == Minecraft.getInstance().player && autoEating) {
            stopAutoEat((Player) event.getEntity());
        }
    }
}