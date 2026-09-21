package net.tablesouls.souls_combat_hud.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.event.ClientModEvents;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SoulsCombatHUD.MODID, value = Dist.CLIENT)
public final class SoulsCombatHUDClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("soulscombathud_client")
                        .then(Commands.literal("reload")
                                .executes(SoulsCombatHUDClientCommands::reloadResources))
        );
    }

    private static int reloadResources(CommandContext<CommandSourceStack> ctx) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();
        ProfilerFiller profiler = minecraft.getProfiler();

        PreparableReloadListener.PreparationBarrier passthrough = new PreparableReloadListener.PreparationBarrier() {
            @Override
            @Nonnull
            public <T> CompletableFuture<T> wait(T pBackgroundResult) {
                return CompletableFuture.completedFuture(pBackgroundResult);
            }
        };

        List<PreparableReloadListener> listeners = List.of(
                ClientModEvents.BOSS_BAR_STYLE_LISTENER,
                ClientModEvents.OXYGEN_BAR_STYLE_LISTENER,
                ClientModEvents.PLAYER_GAUGE_STYLE_LISTENER,
                ClientModEvents.PARTY_GAUGE_STYLE_LISTENER
        );

        for (PreparableReloadListener listener : listeners) {
            listener.reload(passthrough, resourceManager, profiler, profiler, Runnable::run, Runnable::run);
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Reloaded HUD resources."),
                false
        );
        return 1;
    }
}