package net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar;

import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecoration;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecorations;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;

public record OxygenBarStyleDefinition(
        int barColor,
        int barBgColor,
        int barReductionColor,
        int textColor,
        boolean textDropShadow,
        ResourceLocation ornamentTexture
) {
    public static final OxygenBarStyleDefinition DEFAULT = new OxygenBarStyleDefinition(
            BarStyle.OXYGEN_STYLE.barColor(),
            BarStyle.OXYGEN_STYLE.barBgColor(),
            BarStyle.OXYGEN_STYLE.barReductionColor(),
            BarStyle.OXYGEN_STYLE.textColor(),
            BarStyle.OXYGEN_STYLE.textDropShadow(),
            null
    );

    public BarStyle toBarStyle() {
        return new BarStyle(barColor, barBgColor, barReductionColor, textColor, textDropShadow);
    }

    public BarDecoration toBarDecoration() {
        return ornamentTexture == null ? null : BarDecorations.DEFAULT.withTexture(ornamentTexture);
    }
}