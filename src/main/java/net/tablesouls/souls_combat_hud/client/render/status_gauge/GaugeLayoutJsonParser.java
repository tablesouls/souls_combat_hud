package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import com.google.gson.JsonObject;

public final class GaugeLayoutJsonParser {
    private GaugeLayoutJsonParser() {}

    public static GaugeLayout parseLayout(JsonObject json, GaugeLayout fallback) {
        boolean enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : fallback.enabled();

        JsonObject offset = json.has("offset") ? json.getAsJsonObject("offset") : new JsonObject();
        int x = offset.has("x") ? offset.get("x").getAsInt() : fallback.x();
        int y = offset.has("y") ? offset.get("y").getAsInt() : fallback.y();

        int size = json.has("size") ? json.get("size").getAsInt() : fallback.size();

        boolean overridePosition = json.has("override_position")
                ? json.get("override_position").getAsBoolean()
                : fallback.overridePosition();

        return new GaugeLayout(enabled, x, y, size, overridePosition);
    }
}