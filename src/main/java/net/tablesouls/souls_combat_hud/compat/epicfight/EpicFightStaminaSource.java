package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class EpicFightStaminaSource implements ResourceSource {

    @Override
    public boolean isAvailable(Player player) {
        if (!EpicFightCompat.LOADED) return false;
        return resolvePatch(player) != null;
    }

    @Override
    public float getCurrent(Player player) {
        PlayerPatch<?> patch = resolvePatch(player);
        return patch != null ? patch.getStamina() : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        PlayerPatch<?> patch = resolvePatch(player);
        return patch != null ? patch.getMaxStamina() : 0.0f;
    }

    private static PlayerPatch<?> resolvePatch(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return EpicFightCapabilities.getServerPlayerPatch(serverPlayer);
        }

        LocalPlayerPatch localPatch = ClientEngine.getInstance().getPlayerPatch();
        if (localPatch != null && localPatch.getOriginal() == player) {
            return localPatch;
        }
        return null;
    }
}