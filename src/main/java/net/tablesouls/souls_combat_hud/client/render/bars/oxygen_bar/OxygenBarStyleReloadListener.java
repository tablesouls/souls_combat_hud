package net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyleJsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class OxygenBarStyleReloadListener extends SimplePreparableReloadListener<JsonObject> {
    private static final ResourceLocation STYLE_PATH =
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "souls_bars/oxygen_bar.json");

    @Override
    protected JsonObject prepare(ResourceManager manager, ProfilerFiller profiler) {
        Optional<Resource> resource = manager.getResource(STYLE_PATH);
        if (resource.isEmpty()) {
            return new JsonObject();
        }

        try (Reader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            SoulsCombatHUD.LOGGER.error("Failed to read oxygen bar style {}", STYLE_PATH, e);
            return new JsonObject();
        }
    }

    @Override
    protected void apply(JsonObject json, ResourceManager manager, ProfilerFiller profiler) {
        try {
            BarStyle style = BarStyleJsonParser.parseBarStyle(json, BarStyle.OXYGEN_STYLE);
            ResourceLocation ornamentTexture = BarStyleJsonParser.parseOrnamentTexture(
                    json, STYLE_PATH.getNamespace(), OxygenBarStyleDefinition.DEFAULT.ornamentTexture());

            OxygenBarStyleRegistry.set(new OxygenBarStyleDefinition(
                    style.barColor(),
                    style.barBgColor(),
                    style.barReductionColor(),
                    style.textColor(),
                    style.textDropShadow(),
                    ornamentTexture
            ));
        } catch (Exception e) {
            SoulsCombatHUD.LOGGER.error("Failed to parse oxygen bar style", e);
            OxygenBarStyleRegistry.set(OxygenBarStyleDefinition.DEFAULT);
        }
    }
}