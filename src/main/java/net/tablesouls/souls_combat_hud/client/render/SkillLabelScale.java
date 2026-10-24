package net.tablesouls.souls_combat_hud.client.render;

public final class SkillLabelScale {
    private static float scale = 1.0f;

    public static void set(float value) {
        scale = value;
    }

    public static void reset() {
        scale = 1.0f;
    }

    public static float get() {
        return scale;
    }
}