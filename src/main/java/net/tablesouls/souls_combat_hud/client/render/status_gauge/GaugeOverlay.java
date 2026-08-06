package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tablesouls.souls_combat_hud.SoulsCombatHUD;
import net.tablesouls.souls_combat_hud.client.render.bars.BarElement;
import net.tablesouls.souls_combat_hud.client.render.bars.BarScaling;
import net.tablesouls.souls_combat_hud.client.render.bars.BarStyle;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.player.PlayerGaugeSubject;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.thirst.ThirstIcon;
import net.tablesouls.souls_combat_hud.client.render.status_gauge.thirst.ThirstIconRegistry;
import net.tablesouls.souls_combat_hud.client.util.PlayerFacePreviewRenderer;
import net.tablesouls.souls_combat_hud.client.util.StatusBarValues;
import net.tablesouls.souls_combat_hud.config.CrestDisplayMode;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.client.util.PlayerModelPreviewRenderer;
import net.tablesouls.souls_combat_hud.compat.TeamProviderRegistry;
import net.tablesouls.souls_combat_hud.config.ThirstSourceMode;
import net.tablesouls.souls_combat_hud.util.ColorHelper;
import net.tablesouls.souls_combat_hud.client.util.TextHelper;
import net.tablesouls.souls_combat_hud.config.SoulsCombatHUDConfig;
import net.tablesouls.souls_combat_hud.config.StatusEffectSortOrder;
import net.tablesouls.souls_combat_hud.accessor.IEffectDurationAccessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class GaugeOverlay implements IGuiOverlay {
    private final GaugeStyleRegistry gaugeStyles;

    public GaugeOverlay() {
        this(GaugeStyleRegistry.PLAYER);
    }

    public GaugeOverlay(GaugeStyleRegistry gaugeStyles) {
        this.gaugeStyles = gaugeStyles;
    }

    private static final ResourceLocation STATUS_GAUGE_TEX =
            ResourceLocation.fromNamespaceAndPath(SoulsCombatHUD.MODID,
                    "textures/gui/status_gauge.png");
    private static final int STATUS_GAUGE_TEX_SIZE = 64;

    private static final ResourceLocation MINECRAFT_ICONS_TEX =
            ResourceLocation.fromNamespaceAndPath("minecraft",
                    "textures/gui/icons.png");
    private static final int MINECRAFT_ICONS_TEX_SIZE = 256;

    private static final int SKIN_TEX_SIZE = 64;
    private static final int FACE_U = 8;
    private static final int FACE_V = 8;
    private static final int FACE_TEX_SIZE = 8;

    private static final int FACE_LAYER_U = 40;
    private static final int FACE_LAYER_V = 8;

    private static final int HEALTH_BAR_MIN_WIDTH = 0;
    private static final int HEALTH_BAR_MAX_WIDTH = 196;
    private static final int HEALTH_BAR_HEIGHT = 5;

    private static final int STAMINA_BAR_MIN_WIDTH = 0;
    private static final int STAMINA_BAR_MAX_WIDTH = 196;
    private static final int STAMINA_BAR_HEIGHT = 5;

    private static final int MANA_BAR_MIN_WIDTH = 0;
    private static final int MANA_BAR_MAX_WIDTH = 196;
    private static final int MANA_BAR_HEIGHT = 5;

    private static final int CREST_SIZE = 32;
    private static final int CREST_U = 0;
    private static final int CREST_V = 0;

    private static final int CREST_DEAD_U = 0;
    private static final int CREST_DEAD_V = 32;

    private static final int ARMOR_ICON_SIZE = 9;
    private static final int ARMOR_ICON_U = 43;
    private static final int ARMOR_ICON_V = 9;

    private static final int HUNGER_ICON_BG_SIZE = 9;
    private static final int HUNGER_ICON_BG_U = 16;
    private static final int HUNGER_ICON_BG_V = 27;

    private static final int HUNGER_ICON_SIZE = 9;
    private static final int HUNGER_ICON_U = 52;
    private static final int HUNGER_ICON_V = 27;

    private static final int PRIVATE_ICON_WIDTH = 12;
    private static final int PRIVATE_ICON_HEIGHT = 8;
    private static final int PRIVATE_ICON_U = 48;
    private static final int PRIVATE_ICON_V = 56;

    private static final int STATUS_SLOT_U = 48;
    private static final int STATUS_SLOT_V = 0;

    private static final int EFFECT_CATEGORY_SIZE = 8;
    private static final int EFFECT_BENEFICIAL_ICON_U = 48;
    private static final int EFFECT_BENEFICIAL_ICON_V = 16;
    private static final int EFFECT_HARMFUL_ICON_U = 56;
    private static final int EFFECT_HARMFUL_ICON_V = 16;
    private static final int EFFECT_NEUTRAL_ICON_U = 48;
    private static final int EFFECT_NEUTRAL_ICON_V = 24;

    private static final int EFFECT_SLOT_SIZE = 12;
    private static final int EFFECT_SLOT_GAP = 4;
    private static final int EFFECT_MAX_SLOT_PER_ROW = 8;
    private static final int EFFECT_ICON_PADDING = 1;
    private static final int EFFECT_TIMER_HEIGHT = 1;
    private static final int EFFECT_FLICKER_DURATION = 200;

    @Override
    public void render(ForgeGui gui,
                       GuiGraphics graphics,
                       float partialTick,
                       int screenWidth,
                       int screenHeight
    ) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        if (!SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.enabled.get()) return;

        AbstractClientPlayer player = mc.player;
        if (player == null) return;

        GaugeSubject subject = new PlayerGaugeSubject(player);

        ElementAnchor overlayAnchor = SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.anchor.get();
        boolean mirrored = overlayAnchor.horizontal() == ElementAnchor.Horizontal.RIGHT;

        int overlayX = overlayAnchor.resolveX(
                screenWidth,
                SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.x.get(),
                0
        );
        int overlayY = overlayAnchor.resolveY(
                screenHeight,
                SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.y.get(),
                0
        );

        GaugeLayout playerNameLayout = this.gaugeStyles.getLayout("player_name", GaugeLayout.DEFAULT);
        GaugeLayout crestLayout = this.gaugeStyles.getLayout("crest", GaugeLayout.DEFAULT);
        GaugeLayout gaugesLayout = this.gaugeStyles.getLayout("gauges", GaugeLayout.DEFAULT);

        int playerNameX = mirrored ? overlayX - playerNameLayout.x() : overlayX + playerNameLayout.x();
        int playerNameY = overlayY + playerNameLayout.y();

        int crestSize = crestLayout.size();
        int crestX = mirrored ? overlayX - crestLayout.x() - crestSize : overlayX + crestLayout.x();
        int crestY = overlayY + crestLayout.y();

        int gaugesX = mirrored ? overlayX - gaugesLayout.x() : overlayX + gaugesLayout.x();
        int gaugesY = overlayY + gaugesLayout.y();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (crestLayout.enabled()) {
            boolean faceRight = !mirrored;
            CrestDisplayMode displayMode = SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.crestDisplayMode.get();
            OptionalInt teamColor = SoulsCombatHUDConfig.STATUS_GAUGE.playerGauge.crestTeamOutline.get()
                    ? TeamProviderRegistry.resolveActiveTeamColor(player)
                    : OptionalInt.empty();
            this.renderCrest(graphics, subject, font, crestX, crestY, crestSize, faceRight, displayMode, teamColor);
        }

        if (subject.isOnline() && gaugesLayout.enabled()) {
            this.renderGaugeRows(graphics, subject, font, gaugesX, gaugesY, mirrored);
        }

        if (playerNameLayout.enabled()) {
            this.renderPlayerName(graphics, subject, font, playerNameX, playerNameY, mirrored);
        }

        RenderSystem.disableBlend();
    }

    private static int applyConstantWidth(int computedWidth) {
        int constant = SoulsCombatHUDConfig.STATUS_GAUGE.statusBars.constantBarWidth.get();
        return (constant > 0 && computedWidth > 0) ? constant : computedWidth;
    }

    private final BarElement healthBar = BarElement.alwaysVisible();
    private final BarElement staminaBar = BarElement.alwaysVisible();
    private final BarElement manaBar = BarElement.alwaysVisible();

    public void renderStatusBars(GuiGraphics graphics, GaugeSubject subject, int x, int y, boolean mirrored) {
        this.renderHealthBar(graphics, subject, x, y, mirrored);
        this.renderStaminaBar(graphics, subject, x, y, mirrored);
        this.renderManaBar(graphics, subject, x, y, mirrored);
    }

    @FunctionalInterface
    private interface BarRowRenderer {
        void render(GuiGraphics graphics, GaugeSubject subject, int x, int y, boolean mirrored);
    }

    private int renderBarRow(
            GuiGraphics graphics, GaugeSubject subject, GaugeLayout layout,
            int anchorX, int anchorY, int cursorY, int barHeight, int rowGap,
            boolean mirrored, boolean available, BarRowRenderer renderer
    ) {
        if (!layout.enabled() || !available) return cursorY;

        if (layout.overridePosition()) {
            renderer.render(graphics, subject, anchorX, anchorY, mirrored);
            return cursorY;
        }

        renderer.render(graphics, subject, anchorX, cursorY, mirrored);
        return cursorY + barHeight + rowGap;
    }

    public int renderGaugeRows(
            GuiGraphics graphics,
            GaugeSubject subject,
            Font font,
            int x, int y,
            boolean mirrored
    ) {
        int rowGap = gaugeStyles.getRowGap();
        int cursorY = y;

        for (GaugeRow row : gaugeStyles.getRowOrder()) {
            switch (row) {
                case HEALTH -> cursorY = renderBarRow(
                        graphics, subject, gaugeStyles.getLayout("health", GaugeLayout.DEFAULT),
                        x, y, cursorY, HEALTH_BAR_HEIGHT, rowGap, mirrored,
                        subject.hasHealthData(), this::renderHealthBar);
                case STAMINA -> cursorY = renderBarRow(
                        graphics, subject, gaugeStyles.getLayout("stamina", GaugeLayout.DEFAULT),
                        x, y, cursorY, STAMINA_BAR_HEIGHT, rowGap, mirrored,
                        subject.hasStamina(), this::renderStaminaBar);
                case MANA -> cursorY = renderBarRow(
                        graphics, subject, gaugeStyles.getLayout("mana", GaugeLayout.DEFAULT),
                        x, y, cursorY, MANA_BAR_HEIGHT, rowGap, mirrored,
                        subject.hasMana(), this::renderManaBar);
                case STATUS_EFFECTS -> {
                    GaugeLayout statusEffectsLayout = gaugeStyles.getLayout("status_effects", GaugeLayout.DEFAULT);
                    if (!statusEffectsLayout.enabled()) continue;

                    List<MobEffectInstance> effects = new ArrayList<>(subject.getStatusEffects());
                    effects.sort(effectSortComparator(SoulsCombatHUDConfig.STATUS_GAUGE.statusEffects.sortOrder.get()));

                    int maxDisplayed = SoulsCombatHUDConfig.STATUS_GAUGE.statusEffects.maxDisplayed.get();
                    if (maxDisplayed > 0 && effects.size() > maxDisplayed) {
                        effects = effects.subList(0, maxDisplayed);
                    }

                    if (effects.isEmpty()) continue; // collapse: no icons, no row consumed

                    boolean overridePosition = statusEffectsLayout.overridePosition();
                    int rowY = overridePosition ? y : cursorY;

                    int effectsX = mirrored ? x - statusEffectsLayout.x() : x + statusEffectsLayout.x();
                    this.renderStatusEffects(graphics, effects, font, effectsX, rowY + statusEffectsLayout.y(), mirrored);

                    boolean affectsRowStack = gaugeStyles != GaugeStyleRegistry.PARTY
                            || SoulsCombatHUDConfig.STATUS_GAUGE.partyGauge.statusEffectsAffectRowLayout.get();

                    if (!overridePosition && affectsRowStack) {
                        int iconRows = (effects.size() + EFFECT_MAX_SLOT_PER_ROW - 1) / EFFECT_MAX_SLOT_PER_ROW;
                        cursorY += iconRows * (EFFECT_SLOT_SIZE + EFFECT_SLOT_GAP) + rowGap;
                    }
                }
            }
        }

        return cursorY;
    }

    private void renderHealthBar(GuiGraphics graphics, GaugeSubject subject, int x, int y, boolean mirrored) {
        boolean available = subject.hasHealthData();
        float maxHealth = subject.getMaxHealth();

        float healthFraction = (available && maxHealth > 0)
                ? Mth.clamp(subject.getHealth() / maxHealth, 0.0f, 1.0f)
                : 0.0f;

        int barWidth = BarScaling.resolveWidth(
                maxHealth,
                StatusBarValues.healthBaseline(),
                StatusBarValues.healthProjectedMax(),
                HEALTH_BAR_MIN_WIDTH,
                HEALTH_BAR_MAX_WIDTH
        );

        barWidth = applyConstantWidth(barWidth);

        GaugeBarRenderer.renderBar(graphics, gaugeStyles, "health", BarStyle.HEALTH,
                healthBar, available, x, y, barWidth, HEALTH_BAR_HEIGHT, healthFraction, mirrored, true,
                subject.getHealth(), available ? maxHealth : -1f, offlineTint(subject));
    }

    private void renderStaminaBar(GuiGraphics graphics, GaugeSubject subject, int x, int y, boolean mirrored) {
        boolean available = subject.hasStamina();

        float staminaFraction = 0.0f;
        int barWidth = STAMINA_BAR_MIN_WIDTH;

        if (available) {
            float maxStamina = subject.getMaxStamina();
            staminaFraction = maxStamina > 0
                    ? Mth.clamp(subject.getStamina() / maxStamina, 0.0f, 1.0f)
                    : 0.0f;
            barWidth = BarScaling.resolveWidth(
                    maxStamina,
                    StatusBarValues.staminaBaseline(subject.getStaminaSourceMode()),
                    StatusBarValues.staminaProjectedMax(subject.getStaminaSourceMode()),
                    STAMINA_BAR_MIN_WIDTH,
                    STAMINA_BAR_MAX_WIDTH
            );
        }

        barWidth = applyConstantWidth(barWidth);

        GaugeBarRenderer.renderBar(graphics, gaugeStyles, "stamina", BarStyle.STAMINA,
                staminaBar, available, x, y, barWidth, STAMINA_BAR_HEIGHT, staminaFraction, mirrored, true,
                subject.getStamina(), available ? subject.getMaxStamina() : -1f, offlineTint(subject));
    }

    private void renderManaBar(GuiGraphics graphics, GaugeSubject subject, int x, int y, boolean mirrored) {
        boolean available = subject.hasMana();

        float manaFraction = 0.0f;
        int barWidth = MANA_BAR_MIN_WIDTH;

        if (available) {
            float maxMana = subject.getMaxMana();
            manaFraction = maxMana > 0
                    ? Mth.clamp(subject.getMana() / maxMana, 0.0f, 1.0f)
                    : 0.0f;
            barWidth = BarScaling.resolveWidth(
                    maxMana,
                    StatusBarValues.manaBaseline(subject.getManaSourceMode()),
                    StatusBarValues.manaProjectedMax(subject.getManaSourceMode()),
                    MANA_BAR_MIN_WIDTH,
                    MANA_BAR_MAX_WIDTH
            );
        }

        barWidth = applyConstantWidth(barWidth);

        GaugeBarRenderer.renderBar(graphics, gaugeStyles, "mana", BarStyle.MANA,
                manaBar, available, x, y, barWidth, MANA_BAR_HEIGHT, manaFraction, mirrored, true,
                subject.getMana(), available ? subject.getMaxMana() : -1f, offlineTint(subject));
    }

    public void renderCrest(
            GuiGraphics graphics,
            GaugeSubject subject,
            Font font,
            int x,
            int y,
            int size,
            boolean faceRight,
            CrestDisplayMode displayMode
    ) {
        this.renderCrest(graphics, subject, font, x, y, size, faceRight, displayMode, OptionalInt.empty());
    }

    public void renderCrest(
            GuiGraphics graphics,
            GaugeSubject subject,
            Font font,
            int x,
            int y,
            int size,
            boolean faceRight,
            CrestDisplayMode displayMode,
            OptionalInt teamColor
    ) {
        boolean dead = subject.isDeadOrDying();
        float tint = offlineTint(subject);

        if (teamColor.isPresent()) {
            this.renderTeamOutline(graphics, x, y, size, teamColor.getAsInt(), tint);
        }

        int crestU = dead ? CREST_DEAD_U : CREST_U;
        int crestV = dead ? CREST_DEAD_V : CREST_V;

        graphics.blit(
                STATUS_GAUGE_TEX,
                x, y, size, size,
                crestU, crestV,
                CREST_SIZE, CREST_SIZE,
                STATUS_GAUGE_TEX_SIZE, STATUS_GAUGE_TEX_SIZE
        );

        Optional<AbstractClientPlayer> renderableEntity = subject.asRenderableEntity();
        boolean useModel = displayMode == CrestDisplayMode.MODEL
                && !subject.isDeadOrDying()
                && renderableEntity.isPresent()
                && PlayerModelPreviewRenderer.isSafeToRender(renderableEntity.get());

        boolean rendered = useModel
                && PlayerModelPreviewRenderer.render(graphics, renderableEntity.get(), x, y, size, faceRight);

        if (!rendered) {
            this.renderFace(graphics, subject, x, y, size);
        }

        GaugeLayout hungerLayout = this.gaugeStyles.getLayout("hunger", GaugeLayout.DEFAULT);
        TextLayout hungerTextLayout = this.gaugeStyles.getTextLayout("hunger", TextLayout.DEFAULT);

        GaugeLayout armorLayout = this.gaugeStyles.getLayout("armor", GaugeLayout.DEFAULT);
        TextLayout armorTextLayout = this.gaugeStyles.getTextLayout("armor", TextLayout.DEFAULT);

        GaugeLayout thirstLayout = this.gaugeStyles.getLayout("thirst", GaugeLayout.DEFAULT);
        TextLayout thirstTextLayout = this.gaugeStyles.getTextLayout("thirst", TextLayout.DEFAULT);

        OptionalInt foodLevel = subject.getFoodLevel();
        if (hungerLayout.enabled() && foodLevel.isPresent()) {
            this.renderHunger(
                    graphics,
                    subject,
                    foodLevel.getAsInt(),
                    font,
                    x, y,
                    size,
                    hungerLayout.x(), hungerLayout.y(),
                    hungerTextLayout
            );
        }

        OptionalInt armorValue = subject.getArmorValue();
        if (armorLayout.enabled() && armorValue.isPresent()) {
            this.renderArmor(graphics,
                    armorValue.getAsInt(),
                    font,
                    x, y,
                    size,
                    armorLayout.x(), armorLayout.y(),
                    armorTextLayout
            );
        }

        if (thirstLayout.enabled() && subject.hasThirst().orElse(false)) {
            this.renderThirst(
                    graphics,
                    subject.getThirst().orElse(0.0),
                    subject.getMaxThirst().orElse(0.0),
                    subject.getThirstSourceMode(),
                    font,
                    x, y,
                    size,
                    thirstLayout.x(), thirstLayout.y(),
                    thirstTextLayout
            );
        }

        GaugeLayout privacyLayout = this.gaugeStyles.getLayout("private", GaugeLayout.DEFAULT);
        if (privacyLayout.enabled() && subject.isPrivate()) {
            this.renderPrivateIndicator(graphics, x, y, privacyLayout.x(), privacyLayout.y());
        }
    }

    private void renderTeamOutline(GuiGraphics graphics, int x, int y, int size, int packedRgb, float tint) {
        int thickness = 1;

        ColorHelper.RGB rgb = ColorHelper.RGB.fromPackedInt(packedRgb);
        int tinted = new ColorHelper.RGB(rgb.r() * tint, rgb.g() * tint, rgb.b() * tint).toPackedInt();
        int argb = 0xFF000000 | tinted;

        graphics.fill(x - thickness, y - thickness, x + size + thickness, y + size + thickness, argb);
    }

    public void renderFace(
            GuiGraphics graphics,
            GaugeSubject subject,
            int x,
            int y,
            int size
    ) {
        ResourceLocation skinTexture = subject.getSkinTexture();
        if (skinTexture == null) return;
        int faceSize = 16;
        int faceX = x + (size - faceSize)/2;
        int faceY = y + (size - faceSize)/2;

        PlayerFacePreviewRenderer.render(
                graphics,
                skinTexture,
                faceX,
                faceY,
                faceSize
        );
    }

    public void renderPlayerName(
            GuiGraphics graphics,
            GaugeSubject subject,
            Font font,
            int anchorX,
            int y,
            boolean mirrored
    ) {
        Component playerName = subject.getDisplayName();
        int x = mirrored ? anchorX - font.width(playerName) : anchorX;
        int color = subject.isOnline() ? 0xFFFFFF : 0x808080;

        graphics.drawString(
                font,
                playerName,
                x,
                y,
                color,
                true
        );
    }

    private static float offlineTint(GaugeSubject subject) {
        return subject.isOnline() ? 1.0f : 0.4f;
    }

    public void renderHunger(
            GuiGraphics graphics,
            GaugeSubject subject,
            int foodLevel,
            Font font,
            int x,
            int y,
            int size,
            int offsetX,
            int offsetY,
            TextLayout textLayout
    ){
        Optional<AbstractClientPlayer> renderableEntity = subject.asRenderableEntity();
        if (renderableEntity.isEmpty()) return;

        boolean hasHungerEffect = renderableEntity.get().hasEffect(MobEffects.HUNGER);
        float foodPercent = foodLevel / 20.0f;

        int cornerX = x + offsetX;
        int cornerY = y + offsetY;

        int iconX = cornerX - HUNGER_ICON_BG_SIZE / 2;
        int iconY = cornerY - HUNGER_ICON_BG_SIZE / 2;

        graphics.blit(
                MINECRAFT_ICONS_TEX,
                iconX, iconY,
                HUNGER_ICON_BG_U, HUNGER_ICON_BG_V,
                HUNGER_ICON_BG_SIZE, HUNGER_ICON_BG_SIZE,
                MINECRAFT_ICONS_TEX_SIZE, MINECRAFT_ICONS_TEX_SIZE
        );

        int hungerU;

        if (foodPercent <= 0.0f) {
            hungerU = 16;
        } else if (foodPercent <= 0.25f) {
            hungerU = 79;
        } else if (foodPercent <= 0.50f) {
            hungerU = 70;
        } else if (foodPercent <= 0.75f) {
            hungerU = 61;
        } else {
            hungerU = HUNGER_ICON_U;
        }

        if (hasHungerEffect) {
            hungerU += 36;
        }

        graphics.blit(
                MINECRAFT_ICONS_TEX,
                iconX, iconY,
                hungerU, HUNGER_ICON_V,
                HUNGER_ICON_SIZE, HUNGER_ICON_SIZE,
                MINECRAFT_ICONS_TEX_SIZE, MINECRAFT_ICONS_TEX_SIZE
        );

        String foodLevelLabel = String.valueOf(foodLevel);
        int foodLevelTextX = TextHelper.resolveTextBaseX(textLayout.anchor(), iconX, HUNGER_ICON_BG_SIZE, font.width(foodLevelLabel)) + textLayout.x();
        int foodLevelTextY = iconY + HUNGER_ICON_BG_SIZE / 2 - font.lineHeight / 2 + textLayout.y();

        graphics.drawString(
                font,
                foodLevelLabel,
                foodLevelTextX,
                foodLevelTextY,
                0xFFFFFF,
                true
        );
    }

    public void renderArmor(
            GuiGraphics graphics,
            int armorLevel,
            Font font,
            int x,
            int y,
            int size,
            int offsetX,
            int offsetY,
            TextLayout textLayout
    ){
        Minecraft mc = Minecraft.getInstance();

        int cornerX = x + offsetX;
        int cornerY = y + offsetY;

        int iconX = cornerX - ARMOR_ICON_SIZE / 2;
        int iconY = cornerY - ARMOR_ICON_SIZE / 2;

        graphics.blit(
                MINECRAFT_ICONS_TEX,
                iconX, iconY,
                ARMOR_ICON_U, ARMOR_ICON_V,
                ARMOR_ICON_SIZE, ARMOR_ICON_SIZE,
                MINECRAFT_ICONS_TEX_SIZE, MINECRAFT_ICONS_TEX_SIZE
        );

        String armorLevelLabel = String.valueOf(armorLevel);
        int armorLevelTextX = TextHelper.resolveTextBaseX(textLayout.anchor(), iconX, ARMOR_ICON_SIZE, font.width(armorLevelLabel)) + textLayout.x();
        int armorLevelTextY = iconY + ARMOR_ICON_SIZE / 2 - font.lineHeight / 2 + textLayout.y();

        graphics.drawString(
                mc.font,
                armorLevelLabel,
                armorLevelTextX,
                armorLevelTextY,
                0xFFFFFF,
                true
        );
    }

    public void renderPrivateIndicator(
            GuiGraphics graphics,
            int x,
            int y,
            int offsetX,
            int offsetY
    ) {
        int cornerX = x + offsetX;
        int cornerY = y + offsetY;

        int iconX = cornerX - PRIVATE_ICON_WIDTH / 2;
        int iconY = cornerY - PRIVATE_ICON_HEIGHT/ 2;

        graphics.blit(
                STATUS_GAUGE_TEX,
                iconX, iconY,
                PRIVATE_ICON_U, PRIVATE_ICON_V,
                PRIVATE_ICON_WIDTH, PRIVATE_ICON_HEIGHT,
                STATUS_GAUGE_TEX_SIZE, STATUS_GAUGE_TEX_SIZE
        );
    }

    public void renderThirst(
            GuiGraphics graphics,
            double thirst,
            double maxThirst,
            ThirstSourceMode sourceMode,
            Font font,
            int x,
            int y,
            int size,
            int offsetX,
            int offsetY,
            TextLayout textLayout
    ) {
        ThirstIcon icon = ThirstIconRegistry.get(sourceMode);
        if (icon == null) return;

        int cornerX = x + offsetX;
        int cornerY = y + offsetY;

        int iconX = cornerX - icon.iconSize() / 2;
        int iconY = cornerY - icon.iconSize() / 2;

        float percent = maxThirst > 0 ? (float) (thirst / maxThirst) : 0.0f;
        int stage = icon.stageForPercent(percent);

        graphics.blit(
                icon.texture(),
                iconX, iconY,
                icon.stageU()[stage], icon.v(),
                icon.iconSize(), icon.iconSize(),
                icon.textureWidth(), icon.textureHeight()
        );

        String thirstLabel = String.valueOf((int) Math.ceil(thirst));
        int thirstTextX = TextHelper.resolveTextBaseX(textLayout.anchor(), iconX, icon.iconSize(), font.width(thirstLabel)) + textLayout.x();
        int thirstTextY = iconY + icon.iconSize() / 2 - font.lineHeight / 2 + textLayout.y();

        graphics.drawString(
                font,
                thirstLabel,
                thirstTextX,
                thirstTextY,
                0xFFFFFF,
                true
        );
    }

    public void renderStatusEffects(
            GuiGraphics graphics,
            List<MobEffectInstance> effects,
            Font font,
            int anchorX, int y,
            boolean mirrored
    ) {
        int col = 0;
        int row = 0;

        for (MobEffectInstance effectInstance : effects) {
            if (col >= EFFECT_MAX_SLOT_PER_ROW) {
                col = 0;
                row++;
            }

            int step = col * (EFFECT_SLOT_SIZE + EFFECT_SLOT_GAP);
            int statusEffectX = mirrored ? anchorX - step - EFFECT_SLOT_SIZE : anchorX + step;
            int statusEffectY = y + row * (EFFECT_SLOT_SIZE + EFFECT_SLOT_GAP);

            renderStatusEffectSlot(
                    graphics,
                    font,
                    effectInstance,
                    statusEffectX, statusEffectY,
                    EFFECT_SLOT_SIZE);
            col++;
        }
    }

    private record EffectCategoryStyle(int color, int u, int v) {}

    private static EffectCategoryStyle getEffectCategoryStyle(MobEffectCategory category) {
        return switch (category) {
            case BENEFICIAL ->   new EffectCategoryStyle(
                    0xFF3AAA4D,
                    EFFECT_BENEFICIAL_ICON_U,
                    EFFECT_BENEFICIAL_ICON_V);
            case HARMFUL ->   new EffectCategoryStyle(
                    0xFFAA3A3A,
                    EFFECT_HARMFUL_ICON_U,
                    EFFECT_HARMFUL_ICON_V
            );
            case NEUTRAL ->   new EffectCategoryStyle(
                    0xFFFFFFFF,
                    EFFECT_NEUTRAL_ICON_U,
                    EFFECT_NEUTRAL_ICON_V
            );
        };
    }

    private void renderStatusEffectSlot(
            GuiGraphics graphics,
            Font font,
            MobEffectInstance effectInstance,
            int x, int y,
            int size
    ) {
        Minecraft mc = Minecraft.getInstance();
        float alpha = 1.0f;

        if (effectInstance.endsWithin(EFFECT_FLICKER_DURATION)) {
            int duration = effectInstance.getDuration();
            int decay = 10 - duration / 20;
            float fade = Mth.clamp((float) duration / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f);
            float wobble = Mth.cos((float) duration * (float) Math.PI / 5.0f) * Mth.clamp((float) decay / 10.0f * 0.25f, 0.0f, 0.25f);
            alpha = fade + wobble;
        }

        EffectCategoryStyle effectStyle = getEffectCategoryStyle(effectInstance.getEffect().getCategory());
        int statusColor = effectStyle.color();
        int categoryIconU = effectStyle.u();
        int categoryIconV = effectStyle.v();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.setShaderTexture(0, STATUS_GAUGE_TEX);
        graphics.blit(
                STATUS_GAUGE_TEX,
                x, y, size, size,
                STATUS_SLOT_U, STATUS_SLOT_V,
                12, 12,
                STATUS_GAUGE_TEX_SIZE, STATUS_GAUGE_TEX_SIZE
        );

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        TextureAtlasSprite sprite = mc.getMobEffectTextures().get(effectInstance.getEffect());
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

        int iconSize = Math.max(2, size - EFFECT_ICON_PADDING * 2);
        int offsetX = x + (size - iconSize) / 2;
        int offsetY = y + (size - iconSize) / 2;

        graphics.blit(offsetX, offsetY, 0, iconSize, iconSize, sprite);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        int categoryIconX = offsetX - (EFFECT_CATEGORY_SIZE/2);
        int categoryIconY = offsetY - (EFFECT_CATEGORY_SIZE/2);

        graphics.blit(
                STATUS_GAUGE_TEX,
                categoryIconX, categoryIconY,
                categoryIconU, categoryIconV,
                EFFECT_CATEGORY_SIZE, EFFECT_CATEGORY_SIZE,
                STATUS_GAUGE_TEX_SIZE, STATUS_GAUGE_TEX_SIZE
        );

        if (effectInstance.getAmplifier() > 0) {
            Component amplifierLabel = Component.literal(TextHelper.toRomanNumeral(effectInstance.getAmplifier() + 1));

            float ampLabelScale = 0.8f;
            int ampLabelX = x + size - font.width(amplifierLabel)/2;
            int ampLabelY = y - (font.lineHeight/2 - 2);

            graphics.pose().pushPose();
            graphics.pose().translate(ampLabelX, ampLabelY, 0);
            graphics.pose().scale(ampLabelScale, ampLabelScale, 1.0f);
            TextHelper.drawOutlinedString(
                    graphics,
                    font,
                    amplifierLabel,
                    0,
                    0,
                    0xFFFFFF,
                    0x000000
            );
            graphics.pose().popPose();
        }

        renderEffectTimer(graphics, effectInstance, statusColor, x, y, size);
    }

    private static float elapsedFraction(MobEffectInstance instance) {
        int maxDuration = ((IEffectDurationAccessor) instance).souls_combat_hud$getMaxDuration();
        if (maxDuration <= 0) return 0f;
        return 1f - Mth.clamp(instance.getDuration() / (float) maxDuration, 0f, 1f);
    }

    private static Comparator<MobEffectInstance> effectSortComparator(StatusEffectSortOrder order) {
        Comparator<MobEffectInstance> byElapsedAscending = Comparator.comparingDouble(GaugeOverlay::elapsedFraction);

        return switch (order) {
            case NEWEST -> byElapsedAscending;
            case OLDEST -> byElapsedAscending.reversed();
            case HARMFUL -> Comparator
                    .comparing((MobEffectInstance i) -> i.getEffect().isBeneficial()) // false (harmful) sorts first
                    .thenComparing(byElapsedAscending);
            case BENEFICIAL -> Comparator
                    .comparing((MobEffectInstance i) -> !i.getEffect().isBeneficial()) // false (beneficial) sorts first
                    .thenComparing(byElapsedAscending);
        };
    }

    private void renderEffectTimer(
            GuiGraphics graphics,
            MobEffectInstance effectInstance,
            int timerColor,
            int x, int y,
            int size
    ) {
        int currentDuration = effectInstance.getDuration();
        int maxDuration = ((IEffectDurationAccessor) effectInstance).souls_combat_hud$getMaxDuration();

        if (currentDuration <= 0 || maxDuration <= 0) {
            return;
        }

        float fraction = Mth.clamp(currentDuration / (float) maxDuration, 0.0f, 1.0f);

        int timerY = y + size - EFFECT_TIMER_HEIGHT + 2;
        graphics.fill(x, timerY, x + size, y + size, 0x88000000);
        int filledWidth = Math.round(size * fraction);
        graphics.fill(x, timerY, x + filledWidth, y + size, timerColor);
    }
}