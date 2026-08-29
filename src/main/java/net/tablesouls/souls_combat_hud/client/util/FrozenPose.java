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
            int deathTime,
            int hurtTime,
            int hurtDuration,
            float yBodyRot,
            float yBodyRotO,
            float yRot,
            float yRotO,
            float xRot,
            float xRotO,
            float yHeadRot,
            float yHeadRotO,
            VehicleSnapshot vehicleSnapshot
    ) {}

    public static Snapshot freeze(AbstractClientPlayer player, float targetBodyYaw, float targetXRot, float rotationDegrees) {
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
                player.deathTime,
                player.hurtTime,
                player.hurtDuration,
                player.yBodyRot,
                player.yBodyRotO,
                player.getYRot(),
                player.yRotO,
                player.getXRot(),
                player.xRotO,
                player.yHeadRot,
                player.yHeadRotO,
                vehicleSnapshot
        );

        player.swinging = false;
        player.swingTime = 0;
        player.attackAnim = 0f;
        player.oAttackAnim = 0f;

        player.setSwimming(false);

        player.deathTime = 0;
        player.hurtTime = 0;
        player.hurtDuration = 0;

        player.yBodyRot = targetBodyYaw;
        player.yBodyRotO = targetBodyYaw;
        player.setYRot(targetBodyYaw);
        player.yRotO = targetBodyYaw;
        player.setXRot(targetXRot);
        player.xRotO = targetXRot;
        player.yHeadRot = targetBodyYaw;
        player.yHeadRotO = targetBodyYaw;

        if (vehicleSnapshot != null) {
            LivingEntity vehicle = vehicleSnapshot.vehicle();
            float vehicleTargetYRot = 180.0f + rotationDegrees * 40.0f;
            float vehicleTargetYBodyRot = 180.0f + rotationDegrees * 20.0f;
            vehicle.setYRot(vehicleTargetYRot);
            vehicle.yRotO = vehicleTargetYRot;
            vehicle.yBodyRot = vehicleTargetYBodyRot;
            vehicle.yBodyRotO = vehicleTargetYBodyRot;
        }

        return snapshot;
    }

    public static void restore(AbstractClientPlayer player, Snapshot snapshot) {
        player.swinging = snapshot.swinging();
        player.swingTime = snapshot.swingTime();
        player.attackAnim = snapshot.attackAnim();
        player.oAttackAnim = snapshot.oAttackAnim();

        player.setSwimming(snapshot.swimming());

        player.deathTime = snapshot.deathTime();
        player.hurtTime = snapshot.hurtTime();
        player.hurtDuration = snapshot.hurtDuration();

        player.yBodyRot = snapshot.yBodyRot();
        player.yBodyRotO = snapshot.yBodyRotO();
        player.setYRot(snapshot.yRot());
        player.yRotO = snapshot.yRotO();
        player.setXRot(snapshot.xRot());
        player.xRotO = snapshot.xRotO();
        player.yHeadRot = snapshot.yHeadRot();
        player.yHeadRotO = snapshot.yHeadRotO();

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