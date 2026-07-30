package net.tablesouls.souls_combat_hud.client.render.bars;

public record BarStyle(
        int barColor,
        int barBgColor,
        int barReductionColor,
        int textColor,
        boolean textDropShadow
) {
    private static final int REDUCTION_YELLOW = 0xFFE8C34A;
    private static final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

    public static final BarStyle BOSS = new BarStyle(
            0xFF5A1D11,
            0x67000000,
            REDUCTION_YELLOW,
            DEFAULT_TEXT_COLOR,
            true
    );
    public static final BarStyle HEALTH = new BarStyle(
            0xFF742626,
            0x67000000,
            REDUCTION_YELLOW,
            DEFAULT_TEXT_COLOR,
            true
    );
    public static final BarStyle STAMINA = new BarStyle(
            0xFF266F32,
            0x67000000,
            REDUCTION_YELLOW,
            DEFAULT_TEXT_COLOR,
            true
    );
    public static final BarStyle MANA = new BarStyle(
            0xFF26486F,
            0x67000000,
            REDUCTION_YELLOW,
            DEFAULT_TEXT_COLOR,
            true
    );

    public static final BarStyle OXYGEN_STYLE = new BarStyle(
            0xFF2C6FA8,
            0x67000000,
            0xFFB0E0F0,
            0xFFFFFFFF,
            true
    );
}