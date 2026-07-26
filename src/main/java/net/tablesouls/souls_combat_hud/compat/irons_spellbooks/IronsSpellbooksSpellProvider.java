package net.tablesouls.souls_combat_hud.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.sounds.ModSounds;
import net.tablesouls.souls_combat_hud.util.SoundHelper;

import java.util.ArrayList;
import java.util.List;

public class IronsSpellbooksSpellProvider {
    public static SpellSelectionManager getManager() {
        if (!IronsSpellbooksCompat.LOADED) return null;
        return ClientMagicData.getSpellSelectionManager();
    }

    public static boolean hasSpells() {
        SpellSelectionManager manager = getManager();
        return manager != null && manager.getSpellCount() > 0;
    }

    public static void cycle() {
        if (!IronsSpellbooksCompat.LOADED) return;
        SpellSelectionManager manager = getManager();
        if (manager == null || manager.getSpellCount() <= 1) {
            return;
        }
        SoundHelper.playUiSound(ModSounds.CYCLE_SPELL.get());
        int next = (manager.getSelectionIndex() + 1) % manager.getSpellCount();
        manager.makeSelection(next);
    }

    public static SpellData getSelectedSpellData() {
        SpellSelectionManager manager = getManager();
        if (manager == null || manager.getSpellCount() == 0) {
            return null;
        }
        return manager.getSelectedSpellData();
    }

    public static ResourceLocation getSelectedSpellIcon() {
        SpellData data = getSelectedSpellData();
        if (data == null || data.getSpell() == null) {
            return null;
        }
        return data.getSpell().getSpellIconResource();
    }

    public static String getSelectedSpellName() {
        SpellData data = getSelectedSpellData();
        if (data == null) {
            return null;
        }
        return data.getDisplayName().getString();
    }

    public static int getSpellCount() {
        SpellSelectionManager manager = getManager();
        return manager == null ? 0 : manager.getSpellCount();
    }

    public static boolean hasMultipleSpells() {
        return getSpellCount() > 1;
    }

    public static SpellData getPreviewSpellData() {
        SpellSelectionManager manager = getManager();
        if (manager == null || manager.getSpellCount() == 0) {
            return null;
        }
        int nextIndex = (manager.getSelectionIndex() + 1) % manager.getSpellCount();
        return manager.getSpellData(nextIndex);
    }

    public static float getSpellCooldownPercent(SpellData data) {
        if (data == null || data.getSpell() == null) {
            return 0.0f;
        }
        return ClientMagicData.getCooldownPercent(data.getSpell());
    }

    public static float getSelectedSpellCooldownPercent() {
        return getSpellCooldownPercent(getSelectedSpellData());
    }

    public static float getPreviewSpellCooldownPercent() {
        return getSpellCooldownPercent(getPreviewSpellData());
    }

    public record SpellPreviewEntry(ResourceLocation icon, float cooldownPercent) {}

    public static List<SpellPreviewEntry> getPreviewSpellEntries(int count) {
        SpellSelectionManager manager = getManager();
        if (manager == null || manager.getSpellCount() <= 1 || count <= 0) {
            return List.of();
        }
        int size = manager.getSpellCount();
        int max = Math.min(count, size - 1);
        int selected = manager.getSelectionIndex();
        List<SpellPreviewEntry> result = new ArrayList<>(max);
        for (int offset = 1; offset <= max; offset++) {
            SpellData data = manager.getSpellData((selected + offset) % size);
            if (data == null || data.getSpell() == null) {
                continue;
            }
            ResourceLocation icon = data.getSpell().getSpellIconResource();
            float cooldown = getSpellCooldownPercent(data);
            result.add(new SpellPreviewEntry(icon, cooldown));
        }
        return result;
    }
}