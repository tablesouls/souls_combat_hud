package net.tablesouls.souls_combat_hud;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public SoulsCombatHUD(FMLJavaModLoadingContext context)
    {
        context.registerConfig(ModConfig.Type.CLIENT, SoulsCombatHUDConfig.CLIENT_SPEC);
        context.registerConfig(ModConfig.Type.SERVER, SoulsCombatHUDConfig.SERVER_SPEC);
        PartyNetwork.register();
        BossNetwork.register();

        if (FTBTeamsCompat.LOADED) {
            FTBTeamsPartyListener.register();
        }

        if (IronsSpellsCompat.LOADED) {
            IronsSpellsSpellCastListener.register();
        }

        context.registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remoteVersion, isServer) -> true)
        );
    }
}