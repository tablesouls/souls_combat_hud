package net.tablesouls.souls_combat_hud.compat;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.config.SourceMode;

import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractResourceSourceRegistry<M extends Enum<M> & SourceMode> {

    private final Map<M, ResourceSource<M>> byMode;
    private volatile M activeMode;

    protected AbstractResourceSourceRegistry(Class<M> modeClass) {
        this.byMode = new EnumMap<>(modeClass);
    }

    protected final void register(ResourceSource<M> source) {
        byMode.put(source.mode(), source);
    }

    public final void setActiveMode(M mode) {
        this.activeMode = mode;
    }

    public final void resetActiveMode() {
        this.activeMode = null;
    }

    public final ResourceSource<M> resolve() {
        M mode = activeMode;
        return mode == null ? null : byMode.get(mode);
    }

    public final boolean isSupportedAtAll() {
        return !byMode.isEmpty();
    }

    public final Resolution<M> resolveServerSide(Player player, M forcedMode) {
        boolean pinned = forcedMode != null && !forcedMode.isAuto();
        for (ResourceSource<M> source : byMode.values()) { // EnumMap iterates in declared/ordinal order
            if (pinned && source.mode() != forcedMode) continue;
            if (source.isAvailable(player)) {
                return new Resolution<>(source.mode(), source);
            }
        }
        return Resolution.none();
    }

    public record Resolution<M extends Enum<M>>(M mode, ResourceSource<M> source) {
        private static final Resolution<?> NONE = new Resolution<>(null, null);

        @SuppressWarnings("unchecked")
        static <M extends Enum<M>> Resolution<M> none() {
            return (Resolution<M>) NONE;
        }
    }
}