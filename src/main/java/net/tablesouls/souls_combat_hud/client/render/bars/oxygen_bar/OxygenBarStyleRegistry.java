package net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar;

public final class OxygenBarStyleRegistry {
    private static OxygenBarStyleDefinition current = OxygenBarStyleDefinition.DEFAULT;

    static void set(OxygenBarStyleDefinition value) {
        current = value;
    }

    public static OxygenBarStyleDefinition get() {
        return current;
    }
}