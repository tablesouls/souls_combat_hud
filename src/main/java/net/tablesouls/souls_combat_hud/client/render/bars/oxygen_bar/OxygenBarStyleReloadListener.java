package net.tablesouls.souls_combat_hud.client.render.bars.oxygen_bar;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyleJsonParser;
import net.tablesouls.souls_combat_hud.client.render.bars.StackedStyleJsonLoader;

public class OxygenBarStyleReloadListener extends SimplePreparableReloadListener<JsonObject> {
    private static final ResourceLocation STYLE_PATH =
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID, "souls_bars/oxygen_bar.json");

    @Override
    protected JsonObject prepare(ResourceManager manager, ProfilerFiller profiler) {
        return StackedStyleJsonLoader.loadMerged(manager, STYLE_PATH,
                (path, e) -> SoulsCombatHUD.LOGGER.error("Failed to read oxygen bar style layer {}", path, e));
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