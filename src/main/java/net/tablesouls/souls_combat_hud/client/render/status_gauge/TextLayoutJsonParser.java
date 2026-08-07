package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import com.google.gson.JsonObject;
import net.tablesouls.souls_combat_hud.client.util.TextAnchor;

public final class TextLayoutJsonParser {
    private TextLayoutJsonParser() {}

    public static TextLayout parseTextLayout(JsonObject json, TextLayout fallback) {
        if (!json.has("text") || !json.get("text").isJsonObject()) {
            return fallback;
        }

        JsonObject textSection = json.getAsJsonObject("text");

        TextAnchor anchor = fallback.anchor();
        if (textSection.has("anchor")) {
            TextAnchor parsed = TextAnchor.byKey(textSection.get("anchor").getAsString());
            if (parsed != null) {
                anchor = parsed;
            }
        }

        JsonObject offset = textSection.has("offset") ? textSection.getAsJsonObject("offset") : new JsonObject();
        int x = offset.has("x") ? offset.get("x").getAsInt() : fallback.x();
        int y = offset.has("y") ? offset.get("y").getAsInt() : fallback.y();

        return new TextLayout(anchor, x, y);
    }
}