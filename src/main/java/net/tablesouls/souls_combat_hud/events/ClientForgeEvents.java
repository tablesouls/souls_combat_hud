package net.tablesouls.souls_combat_hud.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksCompat;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.util.AutoConsumeHelper;
import net.tablesouls.souls_combat_hud.util.slots.ConsumableSlotManager;
import net.tablesouls.souls_combat_hud.registry.ModKeyBindings;
import net.tablesouls.souls_combat_hud.util.SoundHelper;
import net.tablesouls.souls_combat_hud.util.slots.WeaponSlotManager;
import net.tablesouls.souls_combat_hud.compat.moreoffhandslots.MoreOffhandSlotsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellbooksSpellProvider;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public class ClientForgeEvents {

    private static boolean autoEating;
    private static boolean autoCastingScroll;
    private static int castingGraceTicks;
    private static ItemStack pendingAutoConsumeStack;
    private static int pendingConsumeDelay;
    private static int previousSlot;
    private static int jumpedToSlot = -1;

    private static void jumpOrCycleWeapon(Player player) {
        if (player.isUsingItem()) return;

        boolean canSwitch = EpicFightCompat.canSwitchHoldingItem(player);
        if (!canSwitch) return;

        int slot = WeaponSlotManager.getNextSlot(player);
        if (slot < 0) return;

        previousSlot = player.getInventory().selected;
        jumpedToSlot = slot;
        player.getInventory().selected = slot;

        SoundHelper.playCycleSound(ModSounds.CYCLE_WEAPON.get());

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static void jumpToCycledConsumable(Player player) {
        int slot = ConsumableSlotManager.getSelectedHotbarSlot(player);
        if (slot < 0) {
            return;
        }

        previousSlot = player.getInventory().selected;
        jumpedToSlot = slot;
        player.getInventory().selected = slot;

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static void restorePreviousSlot() {
        if (jumpedToSlot < 0) {
            return;
        }

        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundSetCarriedItemPacket(previousSlot));
        }

        jumpedToSlot = -1;
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

        if (ModKeyBindings.CYCLE_WEAPON.consumeClick()) {
            jumpOrCycleWeapon(player);
        }

        while (ModKeyBindings.CYCLE_OFFHAND.consumeClick()) {
            MoreOffhandSlotsCompat.cycleOffhand(true);
        }

        while (ModKeyBindings.CYCLE_CONSUMABLE.consumeClick()) {
            ConsumableSlotManager.cycle(player);
            if (SoulsCombatHUDConfig.EQUIPMENT_HUD.cycleConsumableSwitch.get()) {
                jumpToCycledConsumable(player);
            }
        }

        while (ModKeyBindings.CYCLE_SPELL.consumeClick()) {
            IronsSpellbooksSpellProvider.cycle();
        }

        if (ModKeyBindings.USE_CONSUMABLE.consumeClick()) {
            jumpToSelectedConsumable(player);
        }

        if (pendingAutoConsumeStack != null) {
            if (pendingConsumeDelay > 0) {
                pendingConsumeDelay--;
            } else {
                beginAutoConsume(player);
                pendingAutoConsumeStack = null;
                pendingConsumeDelay = -1;
            }
        }

        if (autoCastingScroll) {
            if (castingGraceTicks > 0) {
                castingGraceTicks--;
            } else if (!IronsSpellbooksCompat.isCasting()) {
                autoCastingScroll = false;
                restorePreviousSlot(player);
            }
        }

        if (autoEating && !player.isUsingItem()) {
            stopAutoEat(player);
        }
    }

    private static void jumpToSelectedConsumable(Player player) {
        if (autoEating) {
            cancelAutoEat(player);
            return;
        }

        int slot = ConsumableSlotManager.getSelectedHotbarSlot(player);

        if (slot < 0) {
            return;
        }

        if (player.getInventory().selected != slot) {
            previousSlot = player.getInventory().selected;
            jumpedToSlot = slot;
            player.getInventory().selected = slot;

            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            if (connection != null) {
                connection.send(new ServerboundSetCarriedItemPacket(slot));
            }

            pendingAutoConsumeStack = player.getMainHandItem();
            pendingConsumeDelay = 2;

            return;
        }

        // If already holding a consumable, dont return
        if (jumpedToSlot != slot) {
            previousSlot = -1;
            jumpedToSlot = -1;
        }
        beginAutoConsume(player);
    }

    private static void beginAutoConsume(Player player) {
        ItemStack stack = player.getMainHandItem();

        Minecraft.getInstance().gameMode.useItem(player, InteractionHand.MAIN_HAND);

        if (IronsSpellbooksCompat.isScroll(stack)) {
            autoCastingScroll = true;
            castingGraceTicks = 3;
            return;
        }

        if (player.isUsingItem()) {
            autoEating = true;
            AutoConsumeHelper.start(stack.getItem());
        }
    }

    private static void restorePreviousSlot(Player player) {
        if (previousSlot < 0) {
            return;
        }

        // If the player manually switched away from the consumable slot, dont return
        if (jumpedToSlot >= 0 && player.getInventory().selected != jumpedToSlot) {
            previousSlot = -1;
            jumpedToSlot = -1;
            return;
        }

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

    @SubscribeEvent
    public static void onStopUsingItem(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity() == Minecraft.getInstance().player && autoEating) {
            stopAutoEat((Player) event.getEntity());
        }
    }
}