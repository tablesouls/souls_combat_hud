package net.tablesouls.souls_combat_hud.client.render.bars;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class BarStyleJsonParser {
    public static BarStyle parseBarStyle(JsonObject styleJson, BarStyle fallback) {
        JsonObject bar = styleJson.has("bar") ? styleJson.getAsJsonObject("bar") : new JsonObject();
        JsonObject text = styleJson.has("text") ? styleJson.getAsJsonObject("text") : new JsonObject();

        int barColor = parseColor(bar, "color", fallback.barColor());
        int barBgColor = parseColor(bar, "background_color", fallback.barBgColor());
        int barReductionColor = parseColor(bar, "reduction_color", fallback.barReductionColor());
        int textColor = parseColor(text, "color", fallback.textColor());
        boolean textDropShadow = text.has("drop_shadow") ? text.get("drop_shadow").getAsBoolean() : fallback.textDropShadow();

        return new BarStyle(barColor, barBgColor, barReductionColor, textColor, textDropShadow);
    }

    public static ResourceLocation parseOrnamentTexture(JsonObject styleJson, String namespace, ResourceLocation fallback) {
        if (!styleJson.has("ornament_texture")) {
            return fallback;
        }
        return ResourceLocation.fromNamespaceAndPath(namespace,
                "textures/gui/sprites/souls_bars/" + GsonHelper.getAsString(styleJson, "ornament_texture") + ".png");
    }

    public static int parseColor(JsonObject json, String key, int fallback) {
        if (!json.has(key)) {
            return fallback;
        }
        String hex = json.get(key).getAsString().replace("#", "");
        long value = Long.parseLong(hex, 16);
        return hex.length() <= 6 ? (int) (0xFF000000L | value) : (int) value;
    }
}