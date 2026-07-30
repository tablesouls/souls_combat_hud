package net.tablesouls.souls_combat_hud.client.render.bars.boss_bar;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class BossBarStyleRegistry {
    private static List<BossBarStyleDefinition> styles = List.of();

    static void setStyles(List<BossBarStyleDefinition> values) {
        styles = values;
    }

    public static BossBarStyleDefinition resolve(Component bossBarName) {
        for (BossBarStyleDefinition style : styles) {
            if (style.matches(bossBarName)) {
                return style;
            }
        }
        return BossBarStyleDefinition.DEFAULT;
    }
}