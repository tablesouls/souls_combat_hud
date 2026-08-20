package net.tablesouls.souls_combat_hud.client.render.status_gauge;

public record PreviewModelLayout(float rotation, int size, int x, int y) {
    public static final PreviewModelLayout DEFAULT = new PreviewModelLayout(340f, 32, 0, 0);
}