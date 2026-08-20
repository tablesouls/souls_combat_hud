package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import net.tablesouls.souls_combat_hud.client.render.bars.BarDecoration;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GaugeStyleRegistry {
    public static final GaugeStyleRegistry PLAYER = new GaugeStyleRegistry();
    public static final GaugeStyleRegistry PARTY = new GaugeStyleRegistry();

    private final Map<String, BarStyle> styles = new HashMap<>();
    private final Map<String, BarDecoration> decorations = new HashMap<>();
    private final Map<String, GaugeLayout> layouts = new HashMap<>();
    private final Map<String, TextLayout> textLayouts = new HashMap<>();
    private final Map<String, PreviewLayout> previewLayouts = new HashMap<>();

    private List<GaugeRow> rowOrder = GaugeRow.DEFAULT_ORDER;
    private int rowGap = 2;

    void set(String key, BarStyle style, BarDecoration decoration) {
        styles.put(key, style);
        decorations.put(key, decoration);
    }

    void setLayout(String key, GaugeLayout layout) {
        layouts.put(key, layout);
    }

    void setTextLayout(String key, TextLayout layout) {
        textLayouts.put(key, layout);
    }

    void setPreviewLayout(String key, PreviewLayout layout) {
        previewLayouts.put(key, layout);
    }

    void setRowOrder(List<GaugeRow> rowOrder) {
        this.rowOrder = rowOrder.isEmpty() ? GaugeRow.DEFAULT_ORDER : List.copyOf(rowOrder);
    }

    void setRowGap(int rowGap) {
        this.rowGap = Math.max(0, rowGap);
    }

    public BarStyle getStyle(String key, BarStyle fallback) {
        return styles.getOrDefault(key, fallback);
    }

    public BarDecoration getDecoration(String key, BarDecoration fallback) {
        return decorations.getOrDefault(key, fallback);
    }

    public GaugeLayout getLayout(String key, GaugeLayout fallback) {
        return layouts.getOrDefault(key, fallback);
    }

    public TextLayout getTextLayout(String key, TextLayout fallback) {
        return textLayouts.getOrDefault(key, fallback);
    }

    public PreviewLayout getPreviewLayout(String key, PreviewLayout fallback) {
        return previewLayouts.getOrDefault(key, fallback);
    }

    public List<GaugeRow> getRowOrder() {
        return rowOrder;
    }

    public int getRowGap() {
        return rowGap;
    }
}