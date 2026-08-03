package net.tablesouls.souls_combat_hud.client.render.bars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonMerge {
    private static final String OVERRIDE_KEY = "override";
    private static final String TYPE_KEY = "type";

    public static JsonObject merge(JsonObject base, JsonObject patch) {
        if (isOverride(patch)) {
            return stripOverrideFlags(patch.deepCopy()).getAsJsonObject();
        }

        JsonObject result = base.deepCopy();
        for (Map.Entry<String, JsonElement> entry : patch.entrySet()) {
            String key = entry.getKey();
            if (key.equals(OVERRIDE_KEY)) {
                continue;
            }

            JsonElement patchValue = entry.getValue();
            JsonElement baseValue = result.get(key);

            if (patchValue.isJsonObject() && baseValue != null && baseValue.isJsonObject()) {
                result.add(key, merge(baseValue.getAsJsonObject(), patchValue.getAsJsonObject()));
            } else if (patchValue.isJsonArray() && baseValue != null && baseValue.isJsonArray()
                    && isTypedObjectArray(patchValue.getAsJsonArray())) {
                result.add(key, mergeTypedArrays(baseValue.getAsJsonArray(), patchValue.getAsJsonArray()));
            } else {
                result.add(key, stripOverrideFlags(patchValue.deepCopy()));
            }
        }
        return result;
    }

    public static JsonObject mergeStack(List<JsonObject> layers) {
        JsonObject result = new JsonObject();
        for (JsonObject layer : layers) {
            result = merge(result, layer);
        }
        return result;
    }

    private static JsonArray mergeTypedArrays(JsonArray base, JsonArray patch) {
        JsonArray result = new JsonArray();
        List<JsonObject> baseObjects = new ArrayList<>();
        for (JsonElement element : base) {
            if (element.isJsonObject()) {
                baseObjects.add(element.getAsJsonObject());
            }
        }

        for (JsonObject baseElement : baseObjects) {
            String type = typeOf(baseElement);
            JsonObject patchElement = findByType(patch, type);
            result.add(patchElement != null ? merge(baseElement, patchElement) : baseElement.deepCopy());
        }

        for (JsonElement element : patch) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject patchElement = element.getAsJsonObject();
            String type = typeOf(patchElement);
            if (findByType(base, type) == null) {
                result.add(stripOverrideFlags(patchElement.deepCopy()));
            }
        }
        return result;
    }

    private static JsonObject findByType(JsonArray array, String type) {
        if (type == null) {
            return null;
        }
        for (JsonElement element : array) {
            if (element.isJsonObject() && type.equals(typeOf(element.getAsJsonObject()))) {
                return element.getAsJsonObject();
            }
        }
        return null;
    }

    private static String typeOf(JsonObject object) {
        return object.has(TYPE_KEY) && object.get(TYPE_KEY).isJsonPrimitive()
                ? object.get(TYPE_KEY).getAsString()
                : null;
    }

    private static boolean isTypedObjectArray(JsonArray array) {
        if (array.isEmpty()) {
            return false;
        }
        for (JsonElement element : array) {
            if (!element.isJsonObject() || typeOf(element.getAsJsonObject()) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOverride(JsonObject object) {
        return object.has(OVERRIDE_KEY)
                && object.get(OVERRIDE_KEY).isJsonPrimitive()
                && object.get(OVERRIDE_KEY).getAsJsonPrimitive().isBoolean()
                && object.get(OVERRIDE_KEY).getAsBoolean();
    }

    private static JsonElement stripOverrideFlags(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            object.remove(OVERRIDE_KEY);
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                stripOverrideFlags(entry.getValue());
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                stripOverrideFlags(child);
            }
        }
        return element;
    }
}