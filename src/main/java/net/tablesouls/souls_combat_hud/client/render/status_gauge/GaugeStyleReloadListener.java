package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecoration;
import net.tablesouls.souls_combat_hud.client.render.bars.BarDecorations;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyleJsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GaugeStyleReloadListener extends SimplePreparableReloadListener<JsonObject> {
    private final ResourceLocation gaugeStylePath;
    private final GaugeStyleRegistry registry;

    public GaugeStyleReloadListener(String fileName, GaugeStyleRegistry registry) {
        this.gaugeStylePath = ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "souls_bars/" + fileName);
        this.registry = registry;
    }

    @Override
    protected JsonObject prepare(ResourceManager manager, ProfilerFiller profiler) {
        Optional<Resource> resource = manager.getResource(gaugeStylePath);
        if (resource.isEmpty()) {
            return new JsonObject();
        }

        try (Reader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
            return GsonHelper.parse(reader);
        } catch (IOException e) {
            SoulsCombatHUD.LOGGER.error("Failed to read player gauge style {}", gaugeStylePath, e);
            return new JsonObject();
        }
    }

    @Override
    protected void apply(JsonObject json, ResourceManager manager, ProfilerFiller profiler) {
        JsonObject crestSection = json.has("crest") ? json.getAsJsonObject("crest") : new JsonObject();
        JsonObject gaugesSection = json.has("gauges") ? json.getAsJsonObject("gauges") : new JsonObject();

        parseLayoutSection(json, "player_name", GaugeLayout.DEFAULT);

        parseLayoutSection(json, "crest", GaugeLayout.DEFAULT);
        parseLayoutSection(crestSection, "hunger", GaugeLayout.DEFAULT);
        parseLayoutSection(crestSection, "armor", GaugeLayout.DEFAULT);

        parseLayoutSection(json, "gauges", GaugeLayout.DEFAULT);

        parseElements(gaugesSection);
        registry.setRowGap(gaugesSection.has("row_gap") ? gaugesSection.get("row_gap").getAsInt() : 2);
    }

    private void parseElements(JsonObject gaugesSection) {
        if (!gaugesSection.has("elements") || !gaugesSection.get("elements").isJsonArray()) {
            registry.setRowOrder(GaugeRow.DEFAULT_ORDER);
            return;
        }

        List<GaugeRow> order = new ArrayList<>();
        for (JsonElement entry : gaugesSection.getAsJsonArray("elements")) {
            if (!entry.isJsonObject()) {
                SoulsCombatHUD.LOGGER.warn("Gauge element is not an object in {}, skipping", gaugeStylePath);
                continue;
            }

            JsonObject element = entry.getAsJsonObject();
            if (!element.has("type")) {
                SoulsCombatHUD.LOGGER.warn("Gauge element missing 'type' in {}, skipping", gaugeStylePath);
                continue;
            }

            String typeKey = element.get("type").getAsString();
            GaugeRow row = GaugeRow.byKey(typeKey);
            if (row == null) {
                SoulsCombatHUD.LOGGER.warn("Unknown gauge element type '{}' in {}, skipping", typeKey, gaugeStylePath);
                continue;
            }

            switch (row) {
                case HEALTH -> parseBarElement(element, "health", BarStyle.HEALTH);
                case STAMINA -> parseBarElement(element, "stamina", BarStyle.STAMINA);
                case MANA -> parseBarElement(element, "mana", BarStyle.MANA);
                case STATUS_EFFECTS -> parseStatusEffectsElement(element);
            }

            order.add(row);
        }
        registry.setRowOrder(order);
    }

    private void parseBarElement(JsonObject element, String registryKey, BarStyle fallbackStyle) {
        try {
            BarStyle style = BarStyleJsonParser.parseBarStyle(element, fallbackStyle);
            ResourceLocation ornament = BarStyleJsonParser.parseOrnamentTexture(
                    element, gaugeStylePath.getNamespace(), BarDecorations.DEFAULT.texture());
            BarDecoration decoration = BarDecorations.DEFAULT.withTexture(ornament);
            GaugeLayout layout = GaugeLayoutJsonParser.parseLayout(element, GaugeLayout.DEFAULT);

            registry.set(registryKey, style, decoration);
            registry.setLayout(registryKey, layout);
        } catch (Exception e) {
            SoulsCombatHUD.LOGGER.error("Failed to parse gauge element '{}' in {}", registryKey, gaugeStylePath, e);
        }
    }

    private void parseStatusEffectsElement(JsonObject element) {
        try {
            GaugeLayout layout = GaugeLayoutJsonParser.parseLayout(element, GaugeLayout.DEFAULT);
            registry.setLayout("status_effects", layout);
        } catch (Exception e) {
            SoulsCombatHUD.LOGGER.error("Failed to parse gauge element 'status_effects' in {}", gaugeStylePath, e);
        }
    }

    private void parseLayoutSection(JsonObject root, String key, GaugeLayout fallback) {
        if (!root.has(key)) {
            registry.setLayout(key, fallback);
            return;
        }

        try {
            JsonObject section = root.getAsJsonObject(key);
            GaugeLayout layout = GaugeLayoutJsonParser.parseLayout(section, fallback);
            registry.setLayout(key, layout);
        } catch (Exception e) {
            SoulsCombatHUD.LOGGER.error("Failed to parse player gauge layout section '{}'", key, e);
        }
    }
}