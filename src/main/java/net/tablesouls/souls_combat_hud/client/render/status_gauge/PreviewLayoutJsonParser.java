package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import com.google.gson.JsonObject;

public final class PreviewLayoutJsonParser {
    private PreviewLayoutJsonParser() {}

    public static PreviewLayout parseLayout(JsonObject json, PreviewLayout fallback) {
        int size = json.has("size") ? json.get("size").getAsInt() : fallback.size();

        JsonObject offset = json.has("offset") ? json.getAsJsonObject("offset") : new JsonObject();
        int x = offset.has("x") ? offset.get("x").getAsInt() : fallback.x();
        int y = offset.has("y") ? offset.get("y").getAsInt() : fallback.y();

        PreviewModelLayout model = json.has("preview_model")
                ? parseModelLayout(json.getAsJsonObject("preview_model"), fallback.model())
                : fallback.model();

        return new PreviewLayout(size, x, y, model);
    }

    private static PreviewModelLayout parseModelLayout(JsonObject json, PreviewModelLayout fallback) {
        float rotation = json.has("rotation") ? json.get("rotation").getAsFloat() : fallback.rotation();
        int size = json.has("size") ? json.get("size").getAsInt() : fallback.size();

        JsonObject offset = json.has("offset") ? json.getAsJsonObject("offset") : new JsonObject();
        int x = offset.has("x") ? offset.get("x").getAsInt() : fallback.x();
        int y = offset.has("y") ? offset.get("y").getAsInt() : fallback.y();

        return new PreviewModelLayout(rotation, size, x, y);
    }
}