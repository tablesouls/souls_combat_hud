package net.tablesouls.souls_combat_hud;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightCompat;
import net.tablesouls.souls_combat_hud.compat.epicfight.EpicFightDodgeListener;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsCompat;
import net.tablesouls.souls_combat_hud.compat.ftbteams.FTBTeamsPartyListener;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsCompat;
import net.tablesouls.souls_combat_hud.compat.irons_spellbooks.IronsSpellsSpellCastListener;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.network.BossNetwork;
import net.tablesouls.souls_combat_hud.party.network.PartyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SoulsCombatHUD.MODID)
public class SoulsCombatHUD
{
    public static final String MODID = "souls_combat_hud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    private static final String NETWORK_VERSION = "5";
    private static volatile ModConfig serverConfig;

    public SoulsCombatHUD(IEventBus modBus, ModContainer container)
    {
        container.registerConfig(ModConfig.Type.CLIENT, SoulsCombatHUDConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, SoulsCombatHUDConfig.SERVER_SPEC);
        modBus.addListener(SoulsCombatHUD::registerPayloads);
        modBus.addListener(SoulsCombatHUD::onConfigLoading);

        if (FTBTeamsCompat.LOADED) {
            FTBTeamsPartyListener.register();
        }

        if (IronsSpellsCompat.LOADED) {
            IronsSpellsSpellCastListener.register();
        }

        if (EpicFightCompat.LOADED) {
            EpicFightDodgeListener.register();
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        PartyNetwork.register(registrar);
        BossNetwork.register(registrar);
    }

    public static ModConfig getServerConfig()
    {
        return serverConfig;
    }

    private static void onConfigLoading(ModConfigEvent.Loading event)
    {
        if (event.getConfig().getSpec() == SoulsCombatHUDConfig.SERVER_SPEC) {
            serverConfig = event.getConfig();
        }
    }
}
