package net.tablesouls.souls_combat_hud.client.render.bars;

import net.minecraft.resources.ResourceLocation;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;

public final class BarDecorations {

    public static final BarDecoration DEFAULT = new BarDecoration(
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID,
                    "textures/gui/sprites/souls_bars/status_bar_ornament.png"),
            16,
            96,
            16,

            0,
            16,
            64,
            80,

            0,
            0,
            0,
            0
    );

    public static final BarDecoration BOSS = new BarDecoration(
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID,
                    "textures/gui/sprites/souls_bars/bossbar_ornament.png"),
            16,
            96,
            16,

            0,
            16,
            64,
            80,

            0,
            0,
            0,
            0
    );
}