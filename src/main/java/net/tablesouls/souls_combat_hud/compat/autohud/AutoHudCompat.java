package net.tablesouls.souls_combat_hud.compat.autohud;

import net.minecraftforge.fml.ModList;

public class AutoHudCompat {
    public static final String MODID = "autohud";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    private static int suppressTicks;

    public static void suppressHotbarReveal() {
        suppressTicks = 3;
    }

    public static boolean isSuppressingHotbarReveal() {
        return suppressTicks > 0;
    }

    public static void tick() {
        if (suppressTicks > 0) suppressTicks--;
    }
}
