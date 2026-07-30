package net.tablesouls.souls_combat_hud.client.util;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class FrozenPose {
    private record VehicleSnapshot(
            LivingEntity vehicle,
            float yRot,
            float yRotO,
            float yBodyRot,
            float yBodyRotO
    ) {}

    public record Snapshot(
            boolean swinging,
            int swingTime,
            float attackAnim,
            float oAttackAnim,
            boolean swimming,
            VehicleSnapshot vehicleSnapshot
    ) {}

    public static Snapshot freeze(AbstractClientPlayer player, float angleX) {
        Entity vehicleEntity = player.getVehicle();
        VehicleSnapshot vehicleSnapshot = null;
        if (vehicleEntity instanceof LivingEntity vehicle) {
            vehicleSnapshot = new VehicleSnapshot(
                    vehicle, vehicle.getYRot(), vehicle.yRotO, vehicle.yBodyRot, vehicle.yBodyRotO
            );
        }

        Snapshot snapshot = new Snapshot(
                player.swinging,
                player.swingTime,
                player.attackAnim,
                player.oAttackAnim,
                player.isSwimming(),
                vehicleSnapshot
        );

        player.swinging = false;
        player.swingTime = 0;
        player.attackAnim = 0f;
        player.oAttackAnim = 0f;

        player.setSwimming(false);

        if (vehicleSnapshot != null) {
            LivingEntity vehicle = vehicleSnapshot.vehicle();
            float targetYRot = 180.0f + angleX * 40.0f;
            float targetYBodyRot = 180.0f + angleX * 20.0f;
            vehicle.setYRot(targetYRot);
            vehicle.yRotO = targetYRot;
            vehicle.yBodyRot = targetYBodyRot;
            vehicle.yBodyRotO = targetYBodyRot;
        }

        return snapshot;
    }

    public static void restore(AbstractClientPlayer player, Snapshot snapshot) {
        player.swinging = snapshot.swinging();
        player.swingTime = snapshot.swingTime();
        player.attackAnim = snapshot.attackAnim();
        player.oAttackAnim = snapshot.oAttackAnim();

        player.setSwimming(snapshot.swimming());

        if (snapshot.vehicleSnapshot() != null) {
            VehicleSnapshot vs = snapshot.vehicleSnapshot();
            LivingEntity vehicle = vs.vehicle();
            vehicle.setYRot(vs.yRot());
            vehicle.yRotO = vs.yRotO();
            vehicle.yBodyRot = vs.yBodyRot();
            vehicle.yBodyRotO = vs.yBodyRotO();
        }
    }
}