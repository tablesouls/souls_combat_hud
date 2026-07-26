package net.tablesouls.souls_combat_hud.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.item.IScroll;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class IronsSpellbooksCompat {
    public static final String MODID = "irons_spellbooks";
    public static final boolean LOADED = ModList.get().isLoaded(MODID);

    public static boolean isScroll(ItemStack stack) {
        return LOADED && stack.getItem() instanceof IScroll;
    }

    public static boolean isCasting() {
        return LOADED && ClientMagicData.isCasting();
    }
}
