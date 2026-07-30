package net.tablesouls.souls_combat_hud.client.render.status_gauge;

public record GaugeLayout(boolean enabled, int x, int y, int size, boolean overridePosition) {
    public static final GaugeLayout DEFAULT = new GaugeLayout(
            true, 0, 0, 32, false
    );
}