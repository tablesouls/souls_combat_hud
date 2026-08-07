package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import net.tablesouls.souls_combat_hud.client.util.TextAnchor;

public record TextLayout(TextAnchor anchor, int x, int y) {
    public static final TextLayout DEFAULT = new TextLayout(TextAnchor.CENTER, 0, 0);
}