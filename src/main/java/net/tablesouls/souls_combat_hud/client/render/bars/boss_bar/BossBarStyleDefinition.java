package net.tablesouls.souls_combat_hud.client.render.bars.boss_bar;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecoration;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecorations;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;

import java.util.List;

public record BossBarStyleDefinition(
        List<String> targetNames,
        int barColor,
        int barBgColor,
        int barReductionColor,
        int textColor,
        boolean textDropShadow,
        ResourceLocation ornamentTexture,
        long disappearDelay
) {
    public static final BossBarStyleDefinition DEFAULT = new BossBarStyleDefinition(
            List.of(),
            0xFF5A1D11,
            0x67000000,
            0xFFE8C34A,
            0xFFFFFFFF,
            true,
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID,
                    "textures/gui/sprites/souls_bars/bossbar_ornament.png"),
            0L
    );

    public boolean matches(Component bossBarName) {
        String comparisonValue = bossBarName.getContents() instanceof TranslatableContents translatable
                ? translatable.getKey()
                : bossBarName.getString();

        return targetNames.contains(comparisonValue);
    }

    public BarStyle toBarStyle() {
        return new BarStyle(barColor, barBgColor, barReductionColor, textColor, textDropShadow);
    }

    public BarDecoration toBarDecoration() {
        return BarDecorations.BOSS.withTexture(ornamentTexture);
    }
}