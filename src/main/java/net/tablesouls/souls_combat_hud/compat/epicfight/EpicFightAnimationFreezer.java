package net.tablesouls.souls_combat_hud.compat.epicfight;

import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EpicFightAnimationFreezer {
    private static final Field DO_NOT_RESET_TIME_FIELD;
    private static final Field COMPOSITE_LAYERS_FIELD;
    private static final Field BASE_LAYER_PRIORITY_FIELD;
    private static final Field LAYER_DISABLED_FIELD;

    static {
        Field field = null;
        try {
            field = AnimationPlayer.class.getDeclaredField("doNotResetTime");
            field.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        DO_NOT_RESET_TIME_FIELD = field;

        Field compositeLayersField = null;
        try {
            compositeLayersField = Layer.BaseLayer.class.getDeclaredField("compositeLayers");
            compositeLayersField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        COMPOSITE_LAYERS_FIELD = compositeLayersField;

        Field baseLayerPriorityField = null;
        try {
            baseLayerPriorityField = Layer.BaseLayer.class.getDeclaredField("baseLayerPriority");
            baseLayerPriorityField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        BASE_LAYER_PRIORITY_FIELD = baseLayerPriorityField;

        Field disabledField = null;
        try {
            disabledField = Layer.class.getDeclaredField("disabled");
            disabledField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
        LAYER_DISABLED_FIELD = disabledField;
    }

    private static boolean readDoNotResetTime(AnimationPlayer player) {
        if (DO_NOT_RESET_TIME_FIELD == null) return false;
        try {
            return DO_NOT_RESET_TIME_FIELD.getBoolean(player);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static void writeDoNotResetTime(AnimationPlayer player, boolean value) {
        if (DO_NOT_RESET_TIME_FIELD == null) return;
        try {
            DO_NOT_RESET_TIME_FIELD.setBoolean(player, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Layer.Priority, Layer> getCompositeLayers(Layer.BaseLayer baseLayer) {
        if (COMPOSITE_LAYERS_FIELD == null) return Collections.emptyMap();
        try {
            return (Map<Layer.Priority, Layer>) COMPOSITE_LAYERS_FIELD.get(baseLayer);
        } catch (IllegalAccessException e) {
            return Collections.emptyMap();
        }
    }

    private static Layer.Priority readBaseLayerPriority(Layer.BaseLayer baseLayer) {
        if (BASE_LAYER_PRIORITY_FIELD == null) return null;
        try {
            return (Layer.Priority) BASE_LAYER_PRIORITY_FIELD.get(baseLayer);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static void writeBaseLayerPriority(Layer.BaseLayer baseLayer, Layer.Priority priority) {
        if (BASE_LAYER_PRIORITY_FIELD == null || priority == null) return;
        try {
            BASE_LAYER_PRIORITY_FIELD.set(baseLayer, priority);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static boolean readLayerDisabled(Layer layer) {
        if (LAYER_DISABLED_FIELD == null) return false;
        try {
            return LAYER_DISABLED_FIELD.getBoolean(layer);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static void writeLayerDisabled(Layer layer, boolean value) {
        if (LAYER_DISABLED_FIELD == null) return;
        try {
            LAYER_DISABLED_FIELD.setBoolean(layer, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    @Deprecated
    public static void setAnimatorPaused(Player player, boolean paused) {
        if (!EpicFightCompat.LOADED) return;
        LivingEntityPatch<?> patch = (LivingEntityPatch<?>) EpicFightCapabilities.getEntityPatch(player, LivingEntityPatch.class);
        if (patch == null) return;
        ClientAnimator animator = patch.getClientAnimator();
        if (animator == null) return;
        animator.setHardPause(paused);
    }

    private static ClientAnimator getClientAnimator(Player player) {
        LivingEntityPatch<?> patch = getPatch(player);
        return patch == null ? null : patch.getClientAnimator();
    }

    private static LivingEntityPatch<?> getPatch(Player player) {
        if (!EpicFightCompat.LOADED) return null;
        return (LivingEntityPatch<?>) EpicFightCapabilities.getEntityPatch(player, LivingEntityPatch.class);
    }

    private static AssetAccessor<? extends StaticAnimation> getValidWeaponIdOverride(Player player) {
        CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(player.getMainHandItem());
        if (cap == null) return null;

        WeaponCategory category = cap.getWeaponCategory();
        if (category == null) return null;

        if (category == CapabilityItem.WeaponCategories.LONGSWORD) {
            return Animations.BIPED_HOLD_LONGSWORD;
        } else if (category == CapabilityItem.WeaponCategories.GREATSWORD) {
            return Animations.BIPED_HOLD_GREATSWORD;
        } else if (category == CapabilityItem.WeaponCategories.TACHI) {
            return Animations.BIPED_HOLD_TACHI;
        }

        return null;
    }

    public static boolean isBaseLayerSafeToRender(Player player) {
        ClientAnimator animator = getClientAnimator(player);
        if (animator == null) return true;

        Layer.BaseLayer baseLayer = animator.baseLayer;

        // Base layer is queried unconditionally by ClientAnimator.getPose(), regardless of its disabled/empty state, so it must always be checked.
        if (!isLayerAnimationSafe(baseLayer)) {
            return false;
        }

        for (Layer.Priority priority : baseLayer.getBaseLayerPriority().highers()) {
            Layer compositeLayer = baseLayer.getLayer(priority);

            // Matches ClientAnimator.getPose()'s own guard: only layers that are enabled and non-empty are actually queried for a pose.
            if (compositeLayer.isOff()) {
                continue;
            }

            if (!isLayerAnimationSafe(compositeLayer)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isLayerAnimationSafe(Layer layer) {
        AssetAccessor<? extends DynamicAnimation> current = layer.animationPlayer.getAnimation();

        if (current == null || current.get() == null) {
            return false;
        }

        return current.get().getAnimationClip() != null;
    }

    private static final class PlayerSnapshot {
        private final AssetAccessor<? extends DynamicAnimation> prevAnim;
        private final float prevElapsedTime;
        private final float prevPrevElapsedTime;
        private final boolean prevReversed;
        private final boolean prevDoNotResetTime;

        private PlayerSnapshot(AnimationPlayer player) {
            this.prevAnim = player.getAnimation();
            this.prevElapsedTime = player.getElapsedTime();
            this.prevPrevElapsedTime = player.getPrevElapsedTime();
            this.prevReversed = player.isReversed();
            this.prevDoNotResetTime = readDoNotResetTime(player);
        }

        private void restore(AnimationPlayer player) {
            player.setPlayAnimation(this.prevAnim);
            player.setElapsedTime(this.prevPrevElapsedTime, this.prevElapsedTime);
            player.setReversed(this.prevReversed);
            writeDoNotResetTime(player, this.prevDoNotResetTime);
        }
    }

    public static final class FrozenAnimation {
        private final Layer.BaseLayer baseLayer;
        private final AnimationPlayer basePlayer;
        private final PlayerSnapshot baseSnapshot;
        private final Layer.Priority prevBaseLayerPriority;
        private final Map<AnimationPlayer, PlayerSnapshot> compositeSnapshots;
        private final Layer idleCompositeLayer;
        private final Map<Layer, Boolean> prevLayerDisabled;
        private final LivingEntityPatch<?> patch;
        private final LivingMotion prevLivingMotion;
        private final LivingMotion prevCompositeMotion;

        private FrozenAnimation(Layer.BaseLayer baseLayer, Map<AnimationPlayer, PlayerSnapshot> compositeSnapshots,
                                Layer idleCompositeLayer, Map<Layer, Boolean> prevLayerDisabled,
                                LivingEntityPatch<?> patch) {
            this.baseLayer = baseLayer;
            this.basePlayer = baseLayer.animationPlayer;
            this.baseSnapshot = new PlayerSnapshot(this.basePlayer);
            this.prevBaseLayerPriority = readBaseLayerPriority(baseLayer);
            this.compositeSnapshots = compositeSnapshots;
            this.idleCompositeLayer = idleCompositeLayer;
            this.prevLayerDisabled = prevLayerDisabled;
            this.patch = patch;
            this.prevLivingMotion = patch != null ? patch.currentLivingMotion : null;
            this.prevCompositeMotion = patch != null ? patch.currentCompositeMotion : null;
        }
    }

    public static FrozenAnimation freezeToIdle(Player player) {
        LivingEntityPatch<?> patch = getPatch(player);
        ClientAnimator animator = patch == null ? null : patch.getClientAnimator();
        if (animator == null) return null;

        AnimationPlayer basePlayer = animator.baseLayer.animationPlayer;

        AssetAccessor<? extends StaticAnimation> validIdle = getValidWeaponIdOverride(player);

        // Some weapons register their idle stance as a composite-layer overlay
        AssetAccessor<? extends StaticAnimation> idleComposite = animator.getCompositeLivingMotion(LivingMotions.IDLE);
        if (idleComposite != null && idleComposite.get().getAnimationClip() == null) {
            // This composite idle's backing asset failed to load (e.g. epicfightx's broken guard-stance registration).
            // prefer epic fight's original animations
            idleComposite = validIdle;
        }
        Layer.Priority idleCompositePriority = idleComposite != null ? idleComposite.get().getPriority() : null;

        Map<AnimationPlayer, PlayerSnapshot> compositeSnapshots = new LinkedHashMap<>();
        Map<Layer, Boolean> prevLayerDisabled = new LinkedHashMap<>();
        Layer idleCompositeLayer = null;
        for (Map.Entry<Layer.Priority, Layer> entry : getCompositeLayers(animator.baseLayer).entrySet()) {
            Layer.Priority priority = entry.getKey();
            Layer layer = entry.getValue();
            AnimationPlayer layerPlayer = layer.animationPlayer;
            compositeSnapshots.put(layerPlayer, new PlayerSnapshot(layerPlayer));
            prevLayerDisabled.put(layer, readLayerDisabled(layer));

            if (idleComposite != null && priority == idleCompositePriority) {
                layerPlayer.setPlayAnimation(idleComposite);
                idleCompositeLayer = layer;
                writeLayerDisabled(layer, false);
            } else {
                layerPlayer.setPlayAnimation(Animations.EMPTY_ANIMATION);
                writeLayerDisabled(layer, true);
            }
            layerPlayer.setElapsedTime(0f, 0f);
        }

        FrozenAnimation snapshot = new FrozenAnimation(animator.baseLayer, compositeSnapshots, idleCompositeLayer, prevLayerDisabled, patch);

        if (patch != null) {
            patch.currentLivingMotion = LivingMotions.IDLE;
            patch.currentCompositeMotion = LivingMotions.IDLE;
        }

        AssetAccessor<? extends StaticAnimation> idle = validIdle != null ? validIdle : animator.getLivingMotion(LivingMotions.IDLE);
        if (idle != null) {
            basePlayer.setPlayAnimation(idle);
            writeBaseLayerPriority(animator.baseLayer, idle.get().getPriority());
        }

        return snapshot;
    }

    public static void restore(Player player, FrozenAnimation snapshot) {
        if (snapshot == null) return;
        ClientAnimator animator = getClientAnimator(player);
        if (animator == null) return;

        snapshot.baseSnapshot.restore(snapshot.basePlayer);
        writeBaseLayerPriority(snapshot.baseLayer, snapshot.prevBaseLayerPriority);
        if (snapshot.patch != null) {
            snapshot.patch.currentLivingMotion = snapshot.prevLivingMotion;
            snapshot.patch.currentCompositeMotion = snapshot.prevCompositeMotion;
        }
        for (Map.Entry<AnimationPlayer, PlayerSnapshot> entry : snapshot.compositeSnapshots.entrySet()) {
            entry.getValue().restore(entry.getKey());
        }
        for (Map.Entry<Layer, Boolean> entry : snapshot.prevLayerDisabled.entrySet()) {
            writeLayerDisabled(entry.getKey(), entry.getValue());
        }
    }
}