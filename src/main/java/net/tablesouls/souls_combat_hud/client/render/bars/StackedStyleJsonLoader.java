package net.tablesouls.souls_combat_hud.client.render.bars;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

public final class StackedStyleJsonLoader {
    public static JsonObject loadMerged(ResourceManager manager, ResourceLocation path, BiConsumer<String, IOException> onError) {
        List<JsonObject> layers = new ArrayList<>();
        List<Resource> stack = manager.getResourceStack(path);

        for (Resource resource : stack) {
            try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                layers.add(GsonHelper.parse(reader));
            } catch (JsonParseException e) {
                onError.accept(path.toString(), new IOException("Invalid JSON in gauge style layer", e));
            } catch (IOException e) {
                onError.accept(path.toString(), e);
            }
        }

        return JsonMerge.mergeStack(layers);
    }
}