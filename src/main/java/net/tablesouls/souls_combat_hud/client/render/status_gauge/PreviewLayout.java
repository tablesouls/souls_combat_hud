package net.tablesouls.souls_combat_hud.client.render.status_gauge;

public record PreviewLayout(int size, int x, int y, PreviewModelLayout model) {
    public static final PreviewLayout DEFAULT = new PreviewLayout(32, 0, 0, PreviewModelLayout.DEFAULT);
}