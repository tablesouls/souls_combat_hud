package net.tablesouls.souls_combat_hud.client.render.bars;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Reads every resource pack's copy of a single-file style JSON (mod default + any resource
 * pack overrides) and deep-merges them via {@link JsonMerge}, so overriding packs only need to
 * ship the fields they want to change (see {@link JsonMerge} for the "override" flag rules).
 */
public final class StackedStyleJsonLoader {
    private StackedStyleJsonLoader() {
    }

    public static JsonObject loadMerged(ResourceManager manager, ResourceLocation path, BiConsumer<String, IOException> onError) {
        List<JsonObject> layers = new ArrayList<>();
        List<Resource> stack = manager.getResourceStack(path);

        for (Resource resource : stack) {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                layers.add(GsonHelper.parse(reader));
            } catch (IOException e) {
                onError.accept(path.toString(), e);
            }
        }

        return JsonMerge.mergeStack(layers);
    }
}