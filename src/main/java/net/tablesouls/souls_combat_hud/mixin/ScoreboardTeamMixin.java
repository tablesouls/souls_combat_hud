package net.tablesouls.souls_combat_hud.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.tablesouls.souls_combat_hud.party.PartyServerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public abstract class ScoreboardTeamMixin {

    @Inject(
            method = "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)Z",
            at = @At("RETURN")
    )
    private void souls_combat_hud$onPlayerJoinedTeam(String username, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (!((Object) this instanceof ServerScoreboard)) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        souls_combat_hud$recomputeTeam(server, team);
    }

    @Inject(
            method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
            at = @At("RETURN")
    )
    private void souls_combat_hud$onPlayerLeftTeam(String username, PlayerTeam team, CallbackInfo ci) {
        if (!((Object) this instanceof ServerScoreboard)) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        souls_combat_hud$recomputeTeam(server, team);

        ServerPlayer departing = server.getPlayerList().getPlayerByName(username);
        if (departing != null) {
            PartyServerEvents.recomputeTrackersFor(departing);
        }
    }

    private static void souls_combat_hud$recomputeTeam(MinecraftServer server, PlayerTeam team) {
        for (String memberName : team.getPlayers()) {
            ServerPlayer member = server.getPlayerList().getPlayerByName(memberName);
            if (member != null) {
                PartyServerEvents.recomputeTrackersFor(member);
            }
        }
    }
}