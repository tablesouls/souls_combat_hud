package net.tablesouls.souls_combat_hud.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.tablesouls.ts_ui_lib.client.util.ElementAnchor;
import net.tablesouls.ts_ui_lib.client.util.ElementOrientation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SoulsCombatHUDConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec SERVER_SPEC;

    private static final Map<StaminaSourceMode, double[]> STAMINA_PRESET_DEFAULTS = new EnumMap<>(StaminaSourceMode.class);
    static {
        STAMINA_PRESET_DEFAULTS.put(StaminaSourceMode.EPIC_FIGHT, new double[]{20, 45, 1.0});
        STAMINA_PRESET_DEFAULTS.put(StaminaSourceMode.PARCOOL, new double[]{2000, 3000, 1.0});
        STAMINA_PRESET_DEFAULTS.put(StaminaSourceMode.PARAGLIDER, new double[]{1000, 3000, 1.0});
        STAMINA_PRESET_DEFAULTS.put(StaminaSourceMode.MINE_AND_SLASH, new double[]{50, 1040, 0.4});
    }

    private static final Map<ManaSourceMode, double[]> MANA_PRESET_DEFAULTS = new EnumMap<>(ManaSourceMode.class);
    static {
        MANA_PRESET_DEFAULTS.put(ManaSourceMode.IRONS_SPELLBOOKS, new double[]{100, 800, 1.0});
        MANA_PRESET_DEFAULTS.put(ManaSourceMode.MINE_AND_SLASH, new double[]{50, 1040, 0.4});
        MANA_PRESET_DEFAULTS.put(ManaSourceMode.MANA_AND_ARTIFICE, new double[]{100, 1600, 0.6});
        MANA_PRESET_DEFAULTS.put(ManaSourceMode.ARS_NOUVEAU, new double[]{100, 1500, 0.7});
    }

    public static final StatsData STATS_DATA;
    public static final ServerPerformance SERVER_PERFORMANCE;
    public static final ServerRestrictions SERVER_RESTRICTIONS;

    public static final SkillOverlay SKILL_OVERLAY;
    public static final EquipmentHud EQUIPMENT_HUD;
    public static final CustomBossbar CUSTOM_BOSSBAR;
    public static final OxygenBar OXYGEN_BAR;
    public static final ExperienceOverlay EXPERIENCE_OVERLAY;
    public static final StatusGauge STATUS_GAUGE;
    public static final Visibility VISIBILITY;
    public static final DebugClient DEBUG_CLIENT;

    static {
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();

        STATS_DATA = new StatsData(serverBuilder);
        SERVER_PERFORMANCE = new ServerPerformance(serverBuilder);
        SERVER_RESTRICTIONS = new ServerRestrictions(serverBuilder);

        SERVER_SPEC = serverBuilder.build();

        SKILL_OVERLAY = new SkillOverlay(clientBuilder);
        EQUIPMENT_HUD = new EquipmentHud(clientBuilder);
        CUSTOM_BOSSBAR = new CustomBossbar(clientBuilder);
        OXYGEN_BAR = new OxygenBar(clientBuilder);
        EXPERIENCE_OVERLAY = new ExperienceOverlay(clientBuilder);
        STATUS_GAUGE = new StatusGauge(clientBuilder);
        VISIBILITY = new Visibility(clientBuilder);
        DEBUG_CLIENT = new DebugClient(clientBuilder);

        CLIENT_SPEC = clientBuilder.build();
    }

    public static class ElementPosition {
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;
        public final ModConfigSpec.ConfigValue<Double> scale;

        ElementPosition(
                ModConfigSpec.Builder builder,
                String path,
                ElementAnchor defaultAnchor,
                int defaultX, int defaultY,
                double defaultScale
        ) {
            builder.push(path);
            anchor = builder.defineEnum("anchor", defaultAnchor);
            x = builder.define("x", defaultX);
            y = builder.define("y", defaultY);
            scale = builder.defineInRange("scale", defaultScale, 0.25, 4.0);
            builder.pop();
        }
    }

    public static class StatThreshold {
        public final ModConfigSpec.IntValue baseline;
        public final ModConfigSpec.IntValue projectedMax;
        public final ModConfigSpec.DoubleValue barWidthCurve;

        StatThreshold(ModConfigSpec.Builder builder, String key,
                      int defaultBaseline, int defaultProjectedMax, double defaultCurveExponent
        ) {
            builder.push(key);

            baseline = builder.defineInRange("baseline", defaultBaseline, 1, Integer.MAX_VALUE);
            projectedMax = builder.defineInRange("projected_max", defaultProjectedMax, 1, Integer.MAX_VALUE);
            barWidthCurve = builder.defineInRange("bar_width_curve", defaultCurveExponent, 0.1, 3.0);
            builder.pop();
        }
    }

    public static class PresetStatThreshold<M extends Enum<M>> {
        public final Map<M, StatThreshold> presets;

        PresetStatThreshold(
                ModConfigSpec.Builder builder, String key, Class<M> modeClass,
                Map<M, double[]> presetDefaults
        ) {
            builder.push(key);
            builder.push("presets");

            Map<M, StatThreshold> built = new EnumMap<>(modeClass);
            for (M mode : modeClass.getEnumConstants()) {
                double[] defaults = presetDefaults.get(mode);
                if (defaults == null) continue;
                built.put(mode, new StatThreshold(
                        builder, mode.name().toLowerCase(Locale.ROOT),
                        (int) defaults[0], (int) defaults[1], defaults[2])
                );
            }
            presets = Collections.unmodifiableMap(built);

            builder.pop();
            builder.pop();
        }
    }

    public static class StatsData {
        public final StatThreshold health;
        public final PresetStatThreshold<StaminaSourceMode> stamina;
        public final PresetStatThreshold<ManaSourceMode> mana;

        StatsData(ModConfigSpec.Builder builder) {
            builder.comment(
                    "Baseline and projected-max values used to scale HUD bar widths.",
                    "You will most likely be editing the server config version of stats data",
                    "Clients can choose whether to force server values or use their client config.",
                    "You need to use the reload command or reload the world to see the updated values."
            ).push("stats_data");

            health = new StatThreshold(builder, "health", 20, 50, 1.0);
            stamina = new PresetStatThreshold<>(builder, "stamina", StaminaSourceMode.class, STAMINA_PRESET_DEFAULTS);
            mana = new PresetStatThreshold<>(builder, "mana", ManaSourceMode.class, MANA_PRESET_DEFAULTS);

            builder.pop();
        }
    }

    public static class ServerPerformance {
        public final ModConfigSpec.IntValue ticksInterval;
        public final ModConfigSpec.BooleanValue updateOnMobEffect;
        public final ModConfigSpec.BooleanValue updateOnEquipmentChange;
        public final ModConfigSpec.BooleanValue updateOnSpellCast;
        public final ModConfigSpec.BooleanValue updateOnDodge;
        public final ModConfigSpec.IntValue minInstantUpdateTicks;
        public final ModConfigSpec.IntValue minHealthUpdateTicks;

        ServerPerformance(ModConfigSpec.Builder builder) {
            builder.push("server_performance");

            ticksInterval = builder
                    .defineInRange("ticks_interval", 10, 1, 1200);

            updateOnMobEffect = builder
                    .comment("Should status effects like Health Boost update instantly for party members")
                    .define("update_on_mob_effect", true);

            updateOnEquipmentChange = builder
                    .comment("Should armor that grants bonus stats update instantly for party members")
                    .define("update_on_equipment_change", true);

            updateOnSpellCast = builder
                    .comment("Should casting spell update stats instantly for party members")
                    .define("update_on_spell_cast", true);

            updateOnDodge = builder
                    .comment("Should stamina-consuming actions (dodging, basic attacks, skills) update stats instantly for party members")
                    .define("update_on_dodge", true);

            minInstantUpdateTicks = builder
                    .comment("Throttle ticks between events that update party stat trackers such as spell casting.")
                    .defineInRange("min_instant_update_ticks", 10, 0, 1200);

            minHealthUpdateTicks = builder
                    .comment("Throttle ticks between health update trackers.")
                    .defineInRange("min_health_update_ticks", 2, 0, 1200);
            builder.pop();
        }
    }

    public static class ServerRestrictions {
        public final ModConfigSpec.IntValue maxTrackedPartyMembers;
        public final ServerSourcePreference serverSourcePreference;
        public final ModConfigSpec.BooleanValue disablePartyTracking;
        public final ModConfigSpec.BooleanValue disableHealthTracking;
        public final ModConfigSpec.BooleanValue disableStaminaTracking;
        public final ModConfigSpec.BooleanValue disableManaTracking;
        public final ModConfigSpec.BooleanValue disableStatusEffectTracking;
        public final ModConfigSpec.IntValue maxTrackedStatusEffects;

        ServerRestrictions(ModConfigSpec.Builder builder) {
            builder.push("server_restrictions");

            serverSourcePreference = new ServerSourcePreference(builder);
            maxTrackedPartyMembers = builder.comment(
                            "Max teammates the server will track per player, 0 to disable")
                    .defineInRange("max_tracked_party_members", 8, 0, 16);

            disablePartyTracking = builder
                    .comment("If true, the server will never track party stats.")
                    .define("disable_party_tracking", false);

            disableHealthTracking = builder
                    .comment("If true, the server will never track party health.")
                    .define("disable_health_tracking", false);

            disableStaminaTracking = builder
                    .comment("If true, the server will never track party stamina.")
                    .define("disable_stamina_tracking", true);

            disableManaTracking = builder
                    .comment("If true, the server will never track party mana.")
                    .define("disable_mana_tracking", false);

            disableStatusEffectTracking = builder
                    .comment("If true, the server will never track party status effects.")
                    .define("disable_status_effect_tracking", false);

            maxTrackedStatusEffects = builder
                    .comment("Max status effects per party member the server will track and send, 0 to disable limit.")
                    .defineInRange("max_tracked_status_effects", 0, 0, 64);
            builder.pop();
        }

        public static class ServerSourcePreference {
            public final ModConfigSpec.EnumValue<StaminaSourceMode> serverStaminaSource;
            public final ModConfigSpec.EnumValue<ManaSourceMode> serverManaSource;
            public final ModConfigSpec.EnumValue<TeamSourceMode> serverTeamSource;

            ServerSourcePreference(ModConfigSpec.Builder builder) {
                builder.push("server_source_preferences");

                serverStaminaSource = builder
                        .defineEnum("server_stamina_source", StaminaSourceMode.AUTO);
                serverManaSource = builder
                        .defineEnum("server_mana_source", ManaSourceMode.AUTO);
                serverTeamSource = builder
                        .comment(
                                "AUTO = prefer FTB Teams otherwise fall back to vanilla scoreboard teams.",
                                "VANILLA / FTB_TEAMS = always use only that source."
                        )
                        .defineEnum("server_team_source", TeamSourceMode.AUTO);
                builder.pop();
            }
        }
    }

    public static class DebugClient {
        public final ModConfigSpec.BooleanValue enabled;

        DebugClient(ModConfigSpec.Builder builder) {
            builder.push("debug_client");

            enabled = builder
                    .comment("Allow the mod to send output to log. Useful for obtaining skill overlay ids.")
                    .define("enabled", false);

            builder.pop();
        }
    }

    public static class SkillOverlay {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue labelScale;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;
        public final SkillSetting skillSetting;
        public final ElementPosition icon;
        public final ElementPosition text;

        SkillOverlay(ModConfigSpec.Builder builder) {
            builder.comment("Skill Overlay").push("skill_overlay");

            enabled = builder
                    .comment("Toggle whether to use custom skill overlay for Epic Fight.")
                    .define("enabled", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_LEFT
            );

            x = builder.define("x", 12);
            y = builder.define("y", 120);
            labelScale = builder
                    .defineInRange("label_scale", 0.75, 0.25, 2.0);
            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);

            skillSetting = new SkillSetting(builder);
            icon = new ElementPosition(
                    builder, "icon", ElementAnchor.CENTER_LEFT, 4, 0, 1.0);
            text = new ElementPosition(
                    builder, "text", ElementAnchor.CENTER_LEFT, 22, 1, 1.0);

            builder.pop();
        }

        public static class SkillSetting {
            public final ModConfigSpec.BooleanValue weaponPassiveSkill;
            public final ModConfigSpec.ConfigValue<List<? extends String>> weaponPassiveSkillBlacklist;
            public final ModConfigSpec.BooleanValue passiveSkills;
            public final ModConfigSpec.ConfigValue<List<? extends String>> passiveSkillsBlacklist;
            public final ModConfigSpec.BooleanValue guardSkill;
            public final ModConfigSpec.ConfigValue<List<? extends String>> guardSkillBlacklist;
            public final ModConfigSpec.BooleanValue identitySkill;
            public final ModConfigSpec.ConfigValue<List<? extends String>> identitySkillBlacklist;

            SkillSetting(ModConfigSpec.Builder builder) {
                builder.push("skill_setting");

                weaponPassiveSkill = builder
                        .comment("Show weapon passive skills. Note that it may break epic fight addons that utilizes special skill overlays.")
                        .define("show_weapon_passive_skill", false);

                weaponPassiveSkillBlacklist = builder
                        .comment("Dont use custom skill overlay for certain weapon passive skills.")
                        .defineList(
                                "show_weapon_passive_skill_blacklist",
                                List.of(
                                        "wom:solar_passive",
                                        "wom:napoleon_passive"
                                ), o -> o instanceof String
                        );

                passiveSkills = builder
                        .comment("Show passive skills.")
                        .define("show_passive_skills", true);

                passiveSkillsBlacklist = builder
                        .defineList(
                                "show_passive_skills_blacklist",
                                List.of(
                                ), o -> o instanceof String
                        );

                guardSkill = builder
                        .comment("Show guard skill.")
                        .define("show_guard_skill", true);

                guardSkillBlacklist = builder
                        .defineList(
                                "show_guard_skill_blacklist",
                                List.of(
                                ), o -> o instanceof String
                        );

                identitySkill = builder
                        .comment("Show identity skill.")
                        .define("show_identity_skill", true);

                identitySkillBlacklist = builder
                        .defineList(
                                "show_identity_skill_blacklist",
                                List.of(
                                ), o -> o instanceof String
                        );

                builder.pop();
            }
        }
    }

    public static class EquipmentHud {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue cycleSound;

        public final Slots slots;

        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        EquipmentHud(ModConfigSpec.Builder builder) {
            builder.comment("Equipment Hud")
                    .push("equipment_hud");

            enabled = builder
                    .comment("Toggle whether to use the custom equipment HUD.")
                    .define("enabled", true);

            cycleSound = builder
                    .comment("Should cycling items play a sound.")
                    .define("cycle_sound", true);

            slots = new Slots(builder);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_LEFT
            );

            x = builder.define("x", 60);
            y = builder.define("y", 60);

            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);

            builder.pop();
        }

        public static class Slots {
            public final WeaponSlot weapon;
            public final OffhandSlot offhand;
            public final ConsumableSlot consumable;
            public final SpellSlot spell;

            Slots(ModConfigSpec.Builder builder) {
                builder.comment("Slots").push("equipment_slots");

                weapon = new WeaponSlot(builder);
                offhand = new OffhandSlot(builder);
                consumable = new ConsumableSlot(builder);
                spell = new SpellSlot(builder);

                builder.pop();
            }

            public static class ItemNameSetting {
                public final ModConfigSpec.BooleanValue enabled;
                public final ModConfigSpec.BooleanValue autoHide;
                public final ModConfigSpec.IntValue holdMillis;
                public final ModConfigSpec.IntValue fadeMillis;

                ItemNameSetting(ModConfigSpec.Builder builder, String key, boolean pEnabled) {
                    builder.push(key);

                    enabled = builder.define("enabled", pEnabled);
                    autoHide = builder
                            .define("auto_hide", false);
                    holdMillis = builder
                            .defineInRange("hold_millis", 2000, 0, 20000);
                    fadeMillis = builder
                            .defineInRange("fade_millis", 400, 0, 5000);

                    builder.pop();
                }
            }

            public static class PreviewSlots {
                public final ModConfigSpec.IntValue maxSlots;
                public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
                public final ModConfigSpec.EnumValue<ElementOrientation> orientation;
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                PreviewSlots(ModConfigSpec.Builder builder,
                             int defaultMaxSlots,
                             ElementAnchor defaultAnchor,
                             ElementOrientation defaultOrientation,
                             int defaultX,
                             int defaultY
                ) {
                    builder.comment("Preview Slots").push("preview_slots");

                    maxSlots = builder.defineInRange("max_slots", defaultMaxSlots, 0, 12);
                    anchor = builder.defineEnum("anchor", defaultAnchor);
                    orientation = builder.defineEnum("orientation", defaultOrientation);
                    x = builder.define("x", defaultX);
                    y = builder.define("y", defaultY);

                    builder.pop();
                }
            }

            public static class WeaponSlot {
                public final ModConfigSpec.BooleanValue enabled;
                public final PreviewSlots previewSlots;
                public final ModConfigSpec.BooleanValue includeCombatPreferred;
                public final ModConfigSpec.ConfigValue<List<? extends String>> weaponsWhitelist;
                public final ModConfigSpec.ConfigValue<List<? extends String>> weaponsBlacklist;
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                WeaponSlot(ModConfigSpec.Builder builder) {
                    builder.push("weapon_slot");

                    enabled = builder.define("enabled", true);
                    previewSlots = new PreviewSlots(
                            builder,
                            0,
                            ElementAnchor.CENTER_RIGHT,
                            ElementOrientation.VERTICAL,
                            0,
                            0
                    );
                    includeCombatPreferred = builder
                            .comment("Include Combat Preferred Items (Epic Fight)")
                            .define("include_combat_prefered", true);
                    weaponsWhitelist = builder
                            .comment("Include items as a weapon")
                            .defineList("weapons_whitelist", List.of(), o -> o instanceof String);
                    weaponsBlacklist = builder
                            .comment("Exclude items as a weapon")
                            .defineList("weapons_blacklist", List.of(), o -> o instanceof String);
                    x = builder.define("x", 28);
                    y = builder.define("y", 0);
                    builder.pop();
                }
            }

            public static class OffhandSlot {
                public final ModConfigSpec.BooleanValue enabled;
                public final PreviewSlots previewSlots;
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                OffhandSlot(ModConfigSpec.Builder builder) {
                    builder.push("offhand_slot");

                    enabled = builder.define("enabled", true);
                    previewSlots = new PreviewSlots(
                            builder,
                            0,
                            ElementAnchor.CENTER_LEFT,
                            ElementOrientation.VERTICAL,
                            0,
                            0
                    );
                    x = builder.define("x", -28);
                    y = builder.define("y", 0);
                    builder.pop();
                }
            }

            public static class ConsumableSlot {
                public final ModConfigSpec.BooleanValue enabled;
                public final ItemNameSetting itemName;
                public final PreviewSlots previewSlots;
                public final ModConfigSpec.BooleanValue cycleConsumableSwitch;
                public final ModConfigSpec.BooleanValue useConsumableOnSelected;
                public final ModConfigSpec.ConfigValue<List<? extends String>> consumableWhitelist;
                public final ModConfigSpec.ConfigValue<List<? extends String>> consumableBlacklist;
                public final ModConfigSpec.ConfigValue<List<? extends String>> autoconsumeBlacklist;
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                ConsumableSlot(ModConfigSpec.Builder builder) {
                    builder.push("consumable_slot");

                    enabled = builder.define("enabled", true);
                    itemName = new ItemNameSetting(builder, "item_name", true);
                    cycleConsumableSwitch = builder
                            .comment("Should cycling your consumables jump to the item.")
                            .define("cycle_consumable_switch", false);
                    useConsumableOnSelected = builder
                            .comment("Should the use consumable key work directly if selected hotbar item is a consumable.")
                            .define("use_consumable_on_selected", false);
                    previewSlots = new PreviewSlots(
                            builder,
                            3,
                            ElementAnchor.BOTTOM_RIGHT,
                            ElementOrientation.HORIZONTAL,
                            0,
                            -16);
                    consumableWhitelist = builder
                            .comment("Include items as a consumable")
                            .defineList("consumables_whitelist", List.of(), o -> o instanceof String);
                    consumableBlacklist = builder
                            .comment("Exclude items as a consumable")
                            .defineList("consumables_blacklist", List.of(), o -> o instanceof String);
                    autoconsumeBlacklist = builder
                            .comment("Exclude items that should autoconsume")
                            .defineList("autoconsume_blacklist", List.of(
                                    "remnantcurios:flask"
                            ), o -> o instanceof String);
                    x = builder.define("x", 0);
                    y = builder.define("y", 18);
                    builder.pop();
                }
            }

            public static class SpellSlot {
                public final ModConfigSpec.BooleanValue enabled;
                public final ItemNameSetting itemName;
                public final PreviewSlots previewSlots;
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                SpellSlot(ModConfigSpec.Builder builder) {
                    builder.push("spell_slot");

                    enabled = builder.define("enabled", true);
                    itemName = new ItemNameSetting(builder, "item_name", true);
                    previewSlots = new PreviewSlots(
                            builder,
                            3,
                            ElementAnchor.TOP_RIGHT,
                            ElementOrientation.HORIZONTAL,
                            0,
                            16);
                    x = builder.define("x", 0);
                    y = builder.define("y", -18);
                    builder.pop();
                }
            }
        }
    }

    public static class StatusGauge {
        public final ClientSourcePreference clientSourcePreference;
        public final PlayerGaugeOverlay playerGauge;
        public final PartyGaugeOverlay partyGauge;
        public final StatusBars statusBars;
        public final StatusEffects statusEffects;
        public final ModConfigSpec.BooleanValue hideStatusFromParty;

        StatusGauge(ModConfigSpec.Builder builder) {
            builder.comment("Status Gauge").push("status_gauge");

            clientSourcePreference = new ClientSourcePreference(builder);
            playerGauge = new PlayerGaugeOverlay(builder);
            partyGauge = new PartyGaugeOverlay(builder);
            statusBars = new StatusBars(builder);
            statusEffects = new StatusEffects(builder);
            hideStatusFromParty = builder
                    .comment("Should party members be able to see your status")
                    .define("hide_status_from_party", false);

            builder.pop();
        }
    }

    public static class ClientSourcePreference {
        public final ModConfigSpec.EnumValue<StaminaSourceMode> clientStaminaSource;
        public final ModConfigSpec.EnumValue<ManaSourceMode> clientManaSource;
        public final ModConfigSpec.EnumValue<ThirstSourceMode> clientThristSource;
        public final ModConfigSpec.BooleanValue ignoreServerStaminaSource;
        public final ModConfigSpec.BooleanValue ignoreServerManaSource;

        ClientSourcePreference(ModConfigSpec.Builder builder) {
            builder
                    .comment("This mostly applies for the player gauge, party gauges will always use the server's preferred source type.")
                    .push("client_source_preferences");

            clientStaminaSource = builder
                    .comment("Ensure ignore stamina source is disabled")
                    .defineEnum("client_stamina_source", StaminaSourceMode.AUTO);
            clientManaSource = builder
                    .comment("Ensure ignore mana source is disabled")
                    .defineEnum("client_mana_source", ManaSourceMode.AUTO);
            clientThristSource = builder
                    .defineEnum("client_thirst_source", ThirstSourceMode.AUTO);

            ignoreServerStaminaSource = builder
                    .define("ignore_server_stamina_source", false);
            ignoreServerManaSource = builder
                    .define("ignore_server_mana_source", false);

            builder.pop();
        }
    }

    public static class StatusBars {
        public final ModConfigSpec.BooleanValue trustServerValues;
        public final ModConfigSpec.BooleanValue showValueText;
        public final ModConfigSpec.IntValue constantBarWidth;

        public final StatThreshold health;
        public final PresetStatThreshold<StaminaSourceMode> stamina;
        public final PresetStatThreshold<ManaSourceMode> mana;

        StatusBars(ModConfigSpec.Builder builder) {
            builder
                    .comment("Baseline and projected-max values used to scale HUD bar widths.")
                    .push("status_bars");

            trustServerValues = builder
                    .comment("Let servers override local baseline and projected max values")
                    .define("force_server_values", true);

            showValueText = builder
                    .comment("Shows current and max value of status bars")
                    .define("show_value_text", false);

            constantBarWidth = builder
                    .comment("Should status bar have equal width. Set 0 to disable.")
                    .defineInRange("constant_bar_width", 0, 0, 512);

            health = new StatThreshold(builder, "health", 20, 50, 1.0);
            stamina = new PresetStatThreshold<>(builder, "stamina", StaminaSourceMode.class, STAMINA_PRESET_DEFAULTS);
            mana = new PresetStatThreshold<>(builder, "mana", ManaSourceMode.class, MANA_PRESET_DEFAULTS);

            builder.pop();
        }
    }

    public static class StatusEffects {
        public final ModConfigSpec.IntValue maxDisplayed;
        public final ModConfigSpec.EnumValue<StatusEffectSortOrder> sortOrder;

        StatusEffects(ModConfigSpec.Builder builder) {
            builder.comment("Status Effects").push("status_effects");

            maxDisplayed = builder
                    .comment("Max status effect icons shown per gauge, 0 to disable.")
                    .defineInRange("max_displayed", 0, 0, 64);

            sortOrder = builder
                    .comment("Order status effect icons are shown in.")
                    .defineEnum("sort_order", StatusEffectSortOrder.NEWEST);

            builder.pop();
        }
    }

    public static class PlayerGaugeOverlay {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue statusBars;
        public final ModConfigSpec.BooleanValue crest;
        public final ModConfigSpec.EnumValue<CrestDisplayMode> crestDisplayMode;
        public final ModConfigSpec.BooleanValue crestTeamOutline;
        public final ModConfigSpec.BooleanValue crestAttributes;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        PlayerGaugeOverlay(ModConfigSpec.Builder builder) {
            builder.comment(
                    "Player Gauge Overlay",
                    "Customize the status gauge in assets/souls_combat_hud/souls_bars/player_gauge.json"
            ).push("player_gauge_overlay");

            enabled = builder
                    .comment("Toggle player gauge overlay")
                    .define("enabled", true);

            statusBars = builder.define("status_bars", true);

            crest = builder.define("crest", true);
            crestDisplayMode = builder.defineEnum("crest_display_mode", CrestDisplayMode.MODEL);
            crestTeamOutline = builder.define("crest_team_outline", true);
            crestAttributes = builder.define("crest_attributes", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.TOP_LEFT
            );

            x = builder.define("x", 24);
            y = builder.define("y", 24);

            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);

            builder.pop();
        }
    }

    public static class PartyGaugeOverlay {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue statusBars;
        public final ModConfigSpec.BooleanValue crest;
        public final ModConfigSpec.BooleanValue crestTeamOutline;
        public final ModConfigSpec.EnumValue<CrestDisplayMode> crestDisplayMode;
        public final ModConfigSpec.IntValue maxDisplayedPartyMembers;
        public final ModConfigSpec.BooleanValue showOfflineMembers;
        public final ModConfigSpec.BooleanValue sortOnlineFirst;
        public final ModConfigSpec.IntValue memberRowGap;
        public final ModConfigSpec.BooleanValue statusEffectsAffectRowLayout;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        PartyGaugeOverlay(ModConfigSpec.Builder builder) {
            builder.comment(
                    "Party Gauge Overlay",
                    "Shows status gauges for your teammates",
                    "Customize the status gauge in assets/souls_combat_hud/souls_bars/party_gauge.json"
            ).push("party_gauge_overlay");

            enabled = builder
                    .comment("Toggle party gauge overlay")
                    .define("enabled", true);

            statusBars = builder.define("status_bars", true);

            crest = builder.define("crest", true);
            crestDisplayMode = builder.defineEnum("crest_display_mode", CrestDisplayMode.MODEL);
            crestTeamOutline = builder.define("crest_team_outline", true);

            maxDisplayedPartyMembers = builder
                    .comment("Maximum amount of party members to display")
                    .defineInRange("max_displayed_party_members", 4, 0, 16);

            showOfflineMembers = builder
                    .comment("Should offline party members be displayed (FTB Teams)")
                    .define("show_offline_members", true);

            sortOnlineFirst = builder
                    .comment("Sort online players first")
                    .define("sort_online_first", true);

            memberRowGap = builder
                    .comment("Gap offset between party member rows")
                    .defineInRange("member_row_gap", 4, 0, 64);

            statusEffectsAffectRowLayout = builder
                    .comment("Should status effects push party member rows.")
                    .define("status_effects_affect_row_layout", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.TOP_LEFT
            );

            x = builder.define("x", 24);
            y = builder.define("y", 76);

            scale = builder.defineInRange("scale", 0.8125, 0.25, 4.0);

            builder.pop();
        }
    }

    public static class ExperienceOverlay {
        public final ModConfigSpec.BooleanValue enabled;
        public final XpIconSetting xpIcon;
        public final XpBarSetting xpBar;
        public final XpTotalTextSetting xpTotalText;
        public final XpLevelTextSetting xpLevelText;
        public final XpGainPopupSetting xpGainPopup;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        ExperienceOverlay(ModConfigSpec.Builder builder) {
            builder.comment("Experience Overlay").push("experience_overlay");

            enabled = builder
                    .comment("Toggle experience overlay")
                    .define("enabled", true);

            xpIcon = new XpIconSetting(builder);
            xpBar = new XpBarSetting(builder);
            xpTotalText = new XpTotalTextSetting(builder);
            xpLevelText = new XpLevelTextSetting(builder);
            xpGainPopup = new XpGainPopupSetting(builder);

            anchor = builder
                    .defineEnum(
                            "anchor",
                            ElementAnchor.BOTTOM_RIGHT
                    );

            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);
            x = builder.define("x", 12);
            y = builder.define("y", 12);


            builder.pop();
        }

        public static class XpIconSetting {
            public final ModConfigSpec.BooleanValue enabled;
            public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ModConfigSpec.ConfigValue<Integer> x;
            public final ModConfigSpec.ConfigValue<Integer> y;

            XpIconSetting(ModConfigSpec.Builder builder) {
                builder.comment("Experience Icon Settings").push("xp_icon");

                enabled = builder
                        .comment("Toggle experience icon")
                        .define("enabled", true);
                anchor = builder.defineEnum(
                        "anchor",
                        ElementAnchor.CENTER_LEFT);
                x = builder.define("x", 2);
                y = builder.define("y", 0);

                builder.pop();
            }
        }

        public static class XpBarSetting {
            public final ModConfigSpec.BooleanValue enabled;

            XpBarSetting(ModConfigSpec.Builder builder) {
                builder.comment("Experience Bar Settings").push("xp_bar");

                enabled = builder
                        .comment("Toggle experience bar")
                        .define("enabled", true);
                builder.pop();
            }
        }

        public static class XpTotalTextSetting {
            public final ModConfigSpec.BooleanValue enabled;
            public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ModConfigSpec.ConfigValue<Integer> x;
            public final ModConfigSpec.ConfigValue<Integer> y;

            XpTotalTextSetting(ModConfigSpec.Builder builder) {
                builder.comment("Experience Total Text Settings").push("xp_total_text");

                enabled = builder
                        .comment("Toggle experience total text")
                        .define("enabled", true);
                anchor = builder.defineEnum(
                        "anchor",
                        ElementAnchor.CENTER_RIGHT);
                x = builder.define("x", 4);
                y = builder.define("y", 0);

                builder.pop();
            }
        }

        public static class XpLevelTextSetting {
            public final ModConfigSpec.BooleanValue enabled;
            public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ModConfigSpec.ConfigValue<Integer> x;
            public final ModConfigSpec.ConfigValue<Integer> y;

            XpLevelTextSetting(ModConfigSpec.Builder builder) {
                builder.comment("Experience Level Text Settings").push("xp_level_text");

                enabled = builder
                        .comment("Toggle experience level text")
                        .define("enabled", true);
                anchor = builder.defineEnum(
                        "anchor",
                        ElementAnchor.CENTER_LEFT
                );
                x = builder.define("x", 12);
                y = builder.define("y", -1);

                builder.pop();
            }
        }

        public static class XpGainPopupSetting {
            public final ModConfigSpec.BooleanValue enabled;
            public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ModConfigSpec.ConfigValue<Integer> x;
            public final ModConfigSpec.ConfigValue<Integer> y;

            XpGainPopupSetting(ModConfigSpec.Builder builder) {
                builder.push("xp_gain_popup");

                enabled = builder.define("enabled", true);
                anchor = builder.defineEnum("anchor", ElementAnchor.TOP_RIGHT);
                x = builder.define("x", 4);
                y = builder.define("y", -12);

                builder.pop();
            }
        }
    }

    public static class CustomBossbar {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.BooleanValue reductionValueText;
        public final ModConfigSpec.BooleanValue ignoreBossNameFormatting;
        public final ModConfigSpec.LongValue disappear_delay;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.ConfigValue<Integer> maxVisible;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> width;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        CustomBossbar(ModConfigSpec.Builder builder) {
            builder.comment("Custom Bossbar")
                    .push("custom_bossbar");

            enabled = builder
                    .comment("Toggle whether to use Souls-like bossbars.")
                    .define("enabled", true);

            reductionValueText = builder
                    .comment("Should the bossbar display the damage value it has taken. This only works if mod is installed on the server.")
                    .define("reduction_value_text", true);

            ignoreBossNameFormatting = builder
                    .comment("Should the bossbar name text allow the text to be colored etc.")
                    .define("ignore_boss_name_formatting", true);

            disappear_delay = builder
                    .comment("Delay in miliseconds before the bossbar disappears",
                            "This is added on to custom bossbar disappear_delay property")
                    .defineInRange("disappear_delay", 0L, 0, Integer.MAX_VALUE);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_CENTER
            );

            maxVisible = builder.define(
                    "max_visible",
                    4
            );

            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);
            width = builder.define("width", 320);
            x = builder.define("x", 0);
            y = builder.define("y", 64);

            builder.pop();
        }
    }

    public static class OxygenBar {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ModConfigSpec.DoubleValue scale;
        public final ModConfigSpec.ConfigValue<Integer> width;
        public final ModConfigSpec.ConfigValue<Integer> x;
        public final ModConfigSpec.ConfigValue<Integer> y;

        OxygenBar(ModConfigSpec.Builder builder) {
            builder.comment("Oxygen Bar")
                    .push("oxygen_bar");

            enabled = builder
                    .comment("Toggle oxygen bar")
                    .define("enabled", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_CENTER
            );

            scale = builder.defineInRange("scale", 1.0, 0.25, 4.0);
            width = builder.define("width", 182);
            x = builder.define("x", 0);
            y = builder.define("y", 48);

            builder.pop();
        }
    }

    public static class Visibility {
        public final MinecraftGuiSetting minecraftGui;
        public final EpicFightGuiSetting epicfightGui;
        public final ThirstGuiSetting thirstGui;
        public final AutoHudSetting autoHud;
        public final ArsNouveauGui arsNouveauGui;

        Visibility(ModConfigSpec.Builder builder) {
            builder.comment("Visibility").push("visibility");

            minecraftGui = new MinecraftGuiSetting(builder);
            epicfightGui = new EpicFightGuiSetting(builder);
            thirstGui = new ThirstGuiSetting(builder);
            autoHud = new AutoHudSetting(builder);
            arsNouveauGui = new ArsNouveauGui(builder);

            builder.pop();
        }

        public static class ThirstGuiSetting {
            public final ModConfigSpec.BooleanValue hideThirst;

            ThirstGuiSetting(ModConfigSpec.Builder builder) {
                builder.push("thirst");

                hideThirst = builder
                        .comment("Hide thirst bar for mods that dont have the config option.")
                        .define("hide_thirst_bar", true);

                builder.pop();
            }
        }

        public static class EpicFightGuiSetting {
            public final ModConfigSpec.BooleanValue hideStaminaBar;

            EpicFightGuiSetting(ModConfigSpec.Builder builder) {
                builder.push("epicfight");

                hideStaminaBar = builder
                        .define("hide_stamina_bar", true);

                builder.pop();
            }
        }

        public static class AutoHudSetting {
            public final ModConfigSpec.BooleanValue hotbarRemainHidden;

            AutoHudSetting(ModConfigSpec.Builder builder) {
                builder.push("autohud");

                hotbarRemainHidden = builder
                        .comment("Should the hotbar remain hidden if you switched items with the equipment binds.")
                        .define("hotbar_remain_hidden", true);
                builder.pop();
            }
        }

        public static class ArsNouveauGui {
            public final ModConfigSpec.BooleanValue hideManaBar;

            ArsNouveauGui(ModConfigSpec.Builder builder) {
                builder.push("ars_nouveau");

                hideManaBar = builder
                        .define("hide_mana_bar", true);
                builder.pop();
            }
        }

        public static class MinecraftGuiSetting {
            public final MinecraftHotbarSetting hotbar;
            public final ModConfigSpec.BooleanValue disableShiftTextOnGamemode;
            public final MinecraftItemNameSetting itemName;
            public final MinecraftRecordOverlay recordOverlay;
            public final ModConfigSpec.BooleanValue hideBossbar;
            public final ModConfigSpec.BooleanValue hidePotionIcons;

            MinecraftGuiSetting(ModConfigSpec.Builder builder) {
                builder.push("minecraft");

                hotbar = new MinecraftHotbarSetting(builder);

                hideBossbar = builder
                        .comment("Hides the vanilla bossbar.")
                        .define("hide_bossbar", true);
                hidePotionIcons = builder
                        .define("hide_potion_icons", true);
                disableShiftTextOnGamemode = builder
                        .comment("Item name text shifts when switching off and on creative mode.")
                        .define("disable_shift_text_on_gamemode", true);
                itemName = new MinecraftItemNameSetting(builder);
                recordOverlay = new MinecraftRecordOverlay(builder);
                builder.pop();
            }

            public static class MinecraftItemNameSetting {
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                MinecraftItemNameSetting(ModConfigSpec.Builder builder) {
                    builder.comment("Vanilla Highlight Item Name Settings").push("item_name");

                    x = builder.define("x", 0);
                    y = builder.define("y", -22);

                    builder.pop();
                }
            }

            public static class MinecraftRecordOverlay {
                public final ModConfigSpec.ConfigValue<Integer> x;
                public final ModConfigSpec.ConfigValue<Integer> y;

                MinecraftRecordOverlay(ModConfigSpec.Builder builder) {
                    builder.comment("Vanilla Action Bar Settings").push("record_overlay");

                    x = builder.define("x", 0);
                    y = builder.define("y", -20);

                    builder.pop();
                }
            }

            public static class MinecraftHotbarSetting {
                public final MinecraftHotbarStatusSetting status;
                public final ModConfigSpec.BooleanValue hideOffhandSlot;
                public final ModConfigSpec.BooleanValue hideMoreOffhandSlots;

                MinecraftHotbarSetting(ModConfigSpec.Builder builder) {
                    builder.comment("Vanilla Hotbar settings").push("hotbar");

                    status = new MinecraftHotbarStatusSetting(builder);
                    hideOffhandSlot = builder.define("hide_offhand_slot", true);
                    hideMoreOffhandSlots = builder
                            .comment("Hides the More Offhand Slots slots in the hotbar.")
                            .define("hide_moreoffhandslots", true);

                    builder.pop();
                }

                public static class MinecraftHotbarStatusSetting {
                    public final MinecraftHotbarStatusBarSetting healthLevel;
                    public final MinecraftHotbarStatusBarSetting armorLevel;
                    public final MinecraftHotbarStatusBarSetting foodLevel;
                    public final MinecraftHotbarStatusBarSetting airLevel;
                    public final MinecraftHotbarStatusBarSetting xpBar;

                    public final ModConfigSpec.BooleanValue hidden;
                    public final ModConfigSpec.ConfigValue<Integer> x;
                    public final ModConfigSpec.ConfigValue<Integer> y;

                    MinecraftHotbarStatusSetting(ModConfigSpec.Builder builder) {
                        builder.comment("Hotbar Status Setting").push("status");

                        hidden = builder.define("hidden", true);
                        x = builder.define("x", 0);
                        y = builder.define("y", -7);

                        healthLevel = new MinecraftHotbarStatusBarSetting(
                                builder, "health_level", true, 0, 0);
                        foodLevel = new MinecraftHotbarStatusBarSetting(
                                builder, "food_level", false, 0, 0);
                        armorLevel = new MinecraftHotbarStatusBarSetting(
                                builder, "armor_level", false, 0, 0);
                        airLevel = new MinecraftHotbarStatusBarSetting(
                                builder, "air_level", true, 0, 0);
                        xpBar = new MinecraftHotbarStatusBarSetting(
                                builder, "xp_level", true, 0, 0);

                        builder.pop();
                    }

                    public static class MinecraftHotbarStatusBarSetting {
                        public final ModConfigSpec.BooleanValue hidden;
                        public final ModConfigSpec.ConfigValue<Integer> x;
                        public final ModConfigSpec.ConfigValue<Integer> y;

                        MinecraftHotbarStatusBarSetting(
                                ModConfigSpec.Builder builder,
                                String key,
                                boolean pHidden,
                                int pX,
                                int pY
                        ) {
                            builder.push(key);

                            hidden = builder.define("hidden", pHidden);
                            x = builder.define("x", pX);
                            y = builder.define("y", pY);

                            builder.pop();
                        }
                    }
                }
            }
        }
    }
}