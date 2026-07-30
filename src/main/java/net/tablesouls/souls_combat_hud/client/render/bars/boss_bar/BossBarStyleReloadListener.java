package net.tablesouls.souls_combat_hud.client.render.bars.boss_bar;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyleJsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BossBarStyleReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public BossBarStyleReloadListener() {
        super(GSON, "souls_bars/entities");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        List<BossBarStyleDefinition> parsed = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            try {
                parsed.add(parse(entry.getKey(), entry.getValue().getAsJsonObject()));
            } catch (Exception e) {
                SoulsCombatHUD.LOGGER.error("Failed to parse boss bar style {}", entry.getKey(), e);
            }
        }

        BossBarStyleRegistry.setStyles(parsed);
    }

    private BossBarStyleDefinition parse(ResourceLocation id, JsonObject json) {
        List<String> targetNames = new ArrayList<>();
        for (JsonElement el : json.getAsJsonArray("target_names")) {
            targetNames.add(el.getAsString());
        }

        BarStyle style = BarStyleJsonParser.parseBarStyle(json, BarStyle.BOSS);
        ResourceLocation ornamentTexture = BarStyleJsonParser.parseOrnamentTexture(
                json, id.getNamespace(), BossBarStyleDefinition.DEFAULT.ornamentTexture());

        return new BossBarStyleDefinition(
                targetNames,
                style.barColor(),
                style.barBgColor(),
                style.barReductionColor(),
                style.textColor(),
                style.textDropShadow(),
                ornamentTexture
        );
    }
}