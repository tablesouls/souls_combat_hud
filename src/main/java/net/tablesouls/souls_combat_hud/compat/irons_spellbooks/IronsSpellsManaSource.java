package net.tablesouls.souls_combat_hud.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;

public class IronsSpellsManaSource implements ResourceSource {

    @Override
    public boolean isAvailable(Player player) {
        return IronsSpellsCompat.LOADED;
    }

    @Override
    public float getCurrent(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return MagicData.getPlayerMagicData(serverPlayer).getMana();
        }
        if (player == Minecraft.getInstance().player) {
            return ClientMagicData.getPlayerMana();
        }
        return 0.0f;
    }

    @Override
    public float getMax(Player player) {
        return (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
    }
}