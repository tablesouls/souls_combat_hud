package net.tablesouls.souls_combat_hud.command;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.config.ManaSourceMode;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;
import net.tablesouls.souls_combat_hud.party.network.PartyNetwork;
import net.tablesouls.souls_combat_hud.party.network.PartyServerEvents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = SoulsCombatHUD.MODID)
public final class SoulsCombatHUDCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("soulscombathud")
                        .requires(source -> source.hasPermission(2)) // op only
                        .then(Commands.literal("reload")
                                .executes(ctx -> reload(ctx.getSource())))
                        .then(Commands.literal("stamina")
                                .then(Commands.literal("mode")
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        enumNames(StaminaSourceMode.class), builder))
                                                .executes(ctx -> setStaminaMode(
                                                        ctx.getSource(), StringArgumentType.getString(ctx, "mode"))))))
                        .then(Commands.literal("mana")
                                .then(Commands.literal("mode")
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                        enumNames(ManaSourceMode.class), builder))
                                                .executes(ctx -> setManaMode(
                                                        ctx.getSource(), StringArgumentType.getString(ctx, "mode"))))))
        );
    }

    private static int reload(CommandSourceStack source) {
        MinecraftServer server = source.getServer();

        Path configFile = server.getWorldPath(LevelResource.ROOT)
                .resolve("serverconfig")
                .resolve(SoulsCombatHUD.MODID + "-server.toml");

        if (!Files.exists(configFile)) {
            source.sendFailure(Component.literal("No server config file found at " + configFile));
            return 0;
        }

        ModConfig modConfig = SoulsCombatHUD.getServerConfig();
        IConfigSpec.ILoadedConfig loaded = modConfig != null ? modConfig.getLoadedConfig() : null;
        if (loaded == null) {
            source.sendFailure(Component.literal("Server config is not loaded yet."));
            return 0;
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(configFile).sync().build()) {
            fileConfig.load();
            loaded.config().putAll(fileConfig);
            SoulsCombatHUDConfig.SERVER_SPEC.acceptConfig(loaded);
        } catch (Exception e) {
            SoulsCombatHUD.LOGGER.error("Failed to reload {} server config", SoulsCombatHUD.MODID, e);
            source.sendFailure(Component.literal("Failed to reload config - check the server log."));
            return 0;
        }

        PartyNetwork.broadcastConfigReload();
        PartyServerEvents.checkTrackingToggles(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PartyServerEvents.recomputeTrackersFor(player);
            PartyServerEvents.forceResyncResourceSources(player);
        }

        source.sendSuccess(
                () -> Component.literal("souls_combat_hud server config reloaded."),
                true
        );
        return 1;
    }

    private static int setStaminaMode(CommandSourceStack source, String rawMode) {
        StaminaSourceMode mode;
        try {
            mode = StaminaSourceMode.valueOf(rawMode.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Unknown stamina mode: " + rawMode
                    + ". Valid values: " + Arrays.toString(StaminaSourceMode.values())));
            return 0;
        }
        return applyForcedSource(source, SoulsCombatHUDConfig.SERVER_RESTRICTIONS.serverSourcePreference.serverStaminaSource, mode, "stamina");
    }

    private static int setManaMode(CommandSourceStack source, String rawMode) {
        ManaSourceMode mode;
        try {
            mode = ManaSourceMode.valueOf(rawMode.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Unknown mana mode: " + rawMode
                    + ". Valid values: " + Arrays.toString(ManaSourceMode.values())));
            return 0;
        }
        return applyForcedSource(source, SoulsCombatHUDConfig.SERVER_RESTRICTIONS.serverSourcePreference.serverManaSource, mode, "mana");
    }

    private static <M extends Enum<M>> int applyForcedSource(
            CommandSourceStack source, ModConfigSpec.EnumValue<M> configValue, M mode, String label) {
        configValue.set(mode);
        SoulsCombatHUDConfig.SERVER_SPEC.save();

        MinecraftServer server = source.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PartyServerEvents.forceResyncResourceSources(player);
        }

        source.sendSuccess(
                () -> Component.literal("Forced " + label + " source to " + mode),
                true
        );
        return 1;
    }

    private static <E extends Enum<E>> List<String> enumNames(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }
}