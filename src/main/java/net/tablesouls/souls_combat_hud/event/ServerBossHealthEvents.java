package net.tablesouls.souls_combat_hud.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.tablesouls.souls_combat_hud.network.BossNetwork;
import net.tablesouls.souls_combat_hud.util.BossEventLookup;

@EventBusSubscriber(modid = "souls_combat_hud")
public class ServerBossHealthEvents {

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        sync(event.getEntity(), event.getEntity().getHealth() - event.getNewDamage());
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        sync(event.getEntity(), event.getEntity().getHealth() + event.getAmount());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        sync(event.getEntity(), 0f);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof LivingEntity living)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (living.level().isClientSide) return;

        BossEvent bossEvent = BossEventLookup.find(living);
        if (bossEvent == null) return;

        BossNetwork.sendHealth(player, bossEvent.getId(), living.getHealth(), living.getMaxHealth());
    }

    private static void sync(LivingEntity entity, float resultingHealth) {
        if (entity.level().isClientSide) return;

        BossEvent bossEvent = BossEventLookup.find(entity);
        if (bossEvent == null) return;

        float max = entity.getMaxHealth();
        BossNetwork.sendHealthToTrackers(entity, bossEvent.getId(), resultingHealth, max);
    }
}