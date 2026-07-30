package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import java.util.List;

public enum GaugeRow {
    HEALTH("health"),
    STAMINA("stamina"),
    MANA("mana"),
    STATUS_EFFECTS("status_effects");

    public static final List<GaugeRow> DEFAULT_ORDER = List.of(HEALTH, STAMINA, MANA, STATUS_EFFECTS);

    private final String key;

    GaugeRow(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static GaugeRow byKey(String key) {
        for (GaugeRow row : values()) {
            if (row.key.equals(key)) return row;
        }
        return null;
    }
}