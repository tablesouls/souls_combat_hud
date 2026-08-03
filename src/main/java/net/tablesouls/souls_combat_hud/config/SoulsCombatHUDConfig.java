package net.tablesouls.souls_combat_hud.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.tablesouls.souls_combat_hud.client.util.ElementAnchor;
import net.tablesouls.souls_combat_hud.client.util.ElementOrientation;

import java.util.List;

public final class SoulsCombatHUDConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

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

    static {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();

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

        CLIENT_SPEC = clientBuilder.build();
    }

    public static class StatThreshold {
        public final ForgeConfigSpec.IntValue baseline;
        public final ForgeConfigSpec.IntValue projectedMax;

        StatThreshold(ForgeConfigSpec.Builder builder, String key, int defaultBaseline, int defaultProjectedMax) {
            builder.push(key);

            baseline = builder.defineInRange("baseline", defaultBaseline, 1, Integer.MAX_VALUE);
            projectedMax = builder.defineInRange("projected_max", defaultProjectedMax, 1, Integer.MAX_VALUE);

            builder.pop();
        }
    }

    public static class StatsData {
        public final StatThreshold health;
        public final StatThreshold stamina;
        public final StatThreshold mana;

        StatsData(ForgeConfigSpec.Builder builder) {
            builder.comment(
                    "Baseline and projected-max values used to scale HUD bar widths.",
                    "Clients can choose whether to trust these via their own status_bars config."
            ).push("stats_data");

            health = new StatThreshold(builder, "health", 20, 50);
            stamina = new StatThreshold(builder, "stamina", 15, 35);
            mana = new StatThreshold(builder, "mana", 100, 800);

            builder.pop();
        }
    }

    public static class ServerPerformance {
        public final ForgeConfigSpec.IntValue ticksInterval;
        public final ForgeConfigSpec.BooleanValue updateOnMobEffect;
        public final ForgeConfigSpec.BooleanValue updateOnEquipmentChange;
        public final ForgeConfigSpec.BooleanValue updateOnSpellCast;
        public final ForgeConfigSpec.BooleanValue updateOnDodge;
        public final ForgeConfigSpec.IntValue minInstantUpdateTicks;
        public final ForgeConfigSpec.IntValue minHealthUpdateTicks;

        ServerPerformance(ForgeConfigSpec.Builder builder) {
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
        public final ForgeConfigSpec.IntValue maxTrackedPartyMembers;
        public final ForgeConfigSpec.EnumValue<TeamSourcePreference> forceTeamSource;
        public final ForgeConfigSpec.BooleanValue disablePartyTracking;
        public final ForgeConfigSpec.BooleanValue disableHealthTracking;
        public final ForgeConfigSpec.BooleanValue disableStaminaTracking;
        public final ForgeConfigSpec.BooleanValue disableManaTracking;
        public final ForgeConfigSpec.BooleanValue disableStatusEffectTracking;
        public final ForgeConfigSpec.IntValue maxTrackedStatusEffects;

        ServerRestrictions(ForgeConfigSpec.Builder builder) {
            builder.push("server_restrictions");

            maxTrackedPartyMembers = builder.comment(
                            "Max teammates the server will track per player, 0 to disable")
                    .defineInRange("max_tracked_party_members", 8, 0, 16);
            forceTeamSource = builder
                    .comment(
                            "AUTO = prefer FTB Teams otherwise fall back to vanilla scoreboard teams.",
                            "VANILLA / FTB_TEAMS = always use only that source."
                    )
                    .defineEnum("force_team_source", TeamSourcePreference.AUTO);

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
    }

    public static class Toggle {
        public final ForgeConfigSpec.BooleanValue enabled;

        Toggle(ForgeConfigSpec.Builder builder, String key, boolean defaultValue) {
            enabled = builder.define(key, defaultValue);
        }

        Toggle(ForgeConfigSpec.Builder builder, String key, boolean defaultValue, String comment) {
            enabled = builder.comment(comment).define(key, defaultValue);
        }
    }

    public static class SkillOverlay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        SkillOverlay(ForgeConfigSpec.Builder builder) {
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

            builder.pop();
        }
    }

    public static class EquipmentHud {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue cycleSound;

        public final Slots slots;

        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        EquipmentHud(ForgeConfigSpec.Builder builder) {
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

            builder.pop();
        }

        public static class Slots {
            public final WeaponSlot weapon;
            public final OffhandSlot offhand;
            public final ConsumableSlot consumable;
            public final SpellSlot spell;

            Slots(ForgeConfigSpec.Builder builder) {
                builder.comment("Slots").push("equipment_slots");

                weapon = new WeaponSlot(builder);
                offhand = new OffhandSlot(builder);
                consumable = new ConsumableSlot(builder);
                spell = new SpellSlot(builder);

                builder.pop();
            }

            public static class PreviewSlots {
                public final ForgeConfigSpec.IntValue maxSlots;
                public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
                public final ForgeConfigSpec.EnumValue<ElementOrientation> orientation;
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                PreviewSlots(ForgeConfigSpec.Builder builder,
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
                public final ForgeConfigSpec.BooleanValue enabled;
                public final PreviewSlots previewSlots;
                public final ForgeConfigSpec.BooleanValue includeCombatPreferred;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> includeWeaponsList;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludeWeaponsList;
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                WeaponSlot(ForgeConfigSpec.Builder builder) {
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
                    includeWeaponsList = builder
                            .comment("Include items as a weapon")
                            .defineList("include_weapons_list", List.of(), o -> o instanceof String);
                    excludeWeaponsList = builder
                            .comment("Exclude items as a weapon")
                            .defineList("exclude_weapons_list", List.of(), o -> o instanceof String);
                    x = builder.define("x", 28);
                    y = builder.define("y", 0);
                    builder.pop();
                }
            }

            public static class OffhandSlot {
                public final ForgeConfigSpec.BooleanValue enabled;
                public final PreviewSlots previewSlots;
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                OffhandSlot(ForgeConfigSpec.Builder builder) {
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
                public final ForgeConfigSpec.BooleanValue enabled;
                public final Toggle name;
                public final PreviewSlots previewSlots;
                public final ForgeConfigSpec.BooleanValue cycleConsumableSwitch;
                public final ForgeConfigSpec.BooleanValue useConsumableOnSelected;
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                ConsumableSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("consumable_slot");

                    enabled = builder.define("enabled", true);
                    name = new Toggle(builder, "name", true);
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
                    x = builder.define("x", 0);
                    y = builder.define("y", 18);
                    builder.pop();
                }
            }

            public static class SpellSlot {
                public final ForgeConfigSpec.BooleanValue enabled;
                public final Toggle name;
                public final PreviewSlots previewSlots;
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                SpellSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("spell_slot");

                    enabled = builder.define("enabled", true);
                    name = new Toggle(builder, "name", true);
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

    public static class StatusBars {
        public final ForgeConfigSpec.BooleanValue trustServerValues;
        public final ForgeConfigSpec.BooleanValue showValueText;
        public final ForgeConfigSpec.IntValue constantBarWidth;

        public final StatThreshold health;
        public final StatThreshold stamina;
        public final StatThreshold mana;

        StatusBars(ForgeConfigSpec.Builder builder) {
            builder
                    .comment("Baseline and projected-max values used to scale HUD bar widths.")
                    .push("status_bars");

            trustServerValues = builder
                    .comment("Let servers override local baseline and projected max values")
                    .define("trust_server_values", true);

            showValueText = builder
                    .comment("Shows current and max value of status bars")
                    .define("show_value_text", false);

            constantBarWidth = builder
                    .comment("Should status bar have equal width. Set 0 to disable.")
                    .defineInRange("constant_bar_width", 0, 0, 512);

            health = new StatThreshold(builder, "health", 20, 50);
            stamina = new StatThreshold(builder, "stamina", 15, 35);
            mana = new StatThreshold(builder, "mana", 100, 800);

            builder.pop();
        }
    }

    public static class StatusGauge {
        public final PlayerGaugeOverlay playerGauge;
        public final PartyGaugeOverlay partyGauge;
        public final StatusBars statusBars;
        public final StatusEffects statusEffects;

        StatusGauge(ForgeConfigSpec.Builder builder) {
            builder.comment("Status Gauge").push("status_gauge");

            playerGauge = new PlayerGaugeOverlay(builder);
            partyGauge = new PartyGaugeOverlay(builder);
            statusBars = new StatusBars(builder);
            statusEffects = new StatusEffects(builder);

            builder.pop();
        }
    }

    public static class StatusEffects {
        public final ForgeConfigSpec.IntValue maxDisplayed;
        public final ForgeConfigSpec.EnumValue<StatusEffectSortOrder> sortOrder;

        StatusEffects(ForgeConfigSpec.Builder builder) {
            builder.comment("Status Effects").push("status_effects");

            maxDisplayed = builder
                    .comment("Max status effect icons shown per gauge, 0 to disable")
                    .defineInRange("max_displayed", 0, 0, 64);

            sortOrder = builder
                    .comment("Order status effect icons are shown in.")
                    .defineEnum("sort_order", StatusEffectSortOrder.NEWEST);

            builder.pop();
        }
    }

    public static class PlayerGaugeOverlay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<CrestDisplayMode> crestDisplayMode;
        public final ForgeConfigSpec.BooleanValue crestTeamOutline;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        PlayerGaugeOverlay(ForgeConfigSpec.Builder builder) {
            builder.comment(
                    "Player Gauge Overlay",
                    "Customize the status gauge in assets/souls_combat_hud/souls_bars/player_gauge.json"
            ).push("player_gauge_overlay");

            enabled = builder
                    .comment("Toggle player gauge overlay")
                    .define("enabled", true);

            crestTeamOutline = builder
                    .define("crest_team_outline", true);

            crestDisplayMode = builder
                    .comment("MODEL can only be render if Epic Fight's COMPUTE SHADER is OFF")
                    .defineEnum("crest_display_mode", CrestDisplayMode.MODEL);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.TOP_LEFT
            );

            x = builder.define("x", 24);
            y = builder.define("y", 24);

            builder.pop();
        }
    }

    public static class PartyGaugeOverlay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue crestTeamOutline;
        public final ForgeConfigSpec.IntValue maxDisplayedPartyMembers;
        public final ForgeConfigSpec.BooleanValue showOfflineMembers;
        public final ForgeConfigSpec.BooleanValue sortOnlineFirst;
        public final ForgeConfigSpec.IntValue memberRowGap;
        public final ForgeConfigSpec.BooleanValue statusEffectsAffectRowLayout;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        PartyGaugeOverlay(ForgeConfigSpec.Builder builder) {
            builder.comment(
                    "Party Gauge Overlay",
                    "Shows status gauges for your teammates",
                    "Customize the status gauge in assets/souls_combat_hud/souls_bars/party_gauge.json"
            ).push("party_gauge_overlay");

            enabled = builder
                    .comment("Toggle party gauge overlay")
                    .define("enabled", true);

            crestTeamOutline = builder
                    .define("crest_team_outline", true);

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

            builder.pop();
        }
    }

    public static class ExperienceOverlay {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final XpIconSetting xpIcon;
        public final XpBarSetting xpBar;
        public final XpTotalTextSetting xpTotalText;
        public final XpLevelTextSetting xpLevelText;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        ExperienceOverlay(ForgeConfigSpec.Builder builder) {
            builder.comment("Experience Overlay").push("experience_overlay");

            enabled = builder
                    .comment("Toggle experience overlay")
                    .define("enabled", true);

            xpIcon = new XpIconSetting(builder);
            xpBar = new XpBarSetting(builder);
            xpTotalText = new XpTotalTextSetting(builder);
            xpLevelText = new XpLevelTextSetting(builder);

            anchor = builder
                    .defineEnum(
                            "anchor",
                            ElementAnchor.BOTTOM_RIGHT
                    );

            x = builder.define("x", 12);
            y = builder.define("y", 12);

            builder.pop();
        }

        public static class XpIconSetting {
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ForgeConfigSpec.ConfigValue<Integer> x;
            public final ForgeConfigSpec.ConfigValue<Integer> y;

            XpIconSetting(ForgeConfigSpec.Builder builder) {
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
            public final ForgeConfigSpec.BooleanValue enabled;

            XpBarSetting(ForgeConfigSpec.Builder builder) {
                builder.comment("Experience Bar Settings").push("xp_bar");

                enabled = builder
                        .comment("Toggle experience bar")
                        .define("enabled", true);
                builder.pop();
            }
        }

        public static class XpTotalTextSetting {
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ForgeConfigSpec.ConfigValue<Integer> x;
            public final ForgeConfigSpec.ConfigValue<Integer> y;

            XpTotalTextSetting(ForgeConfigSpec.Builder builder) {
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
            public final ForgeConfigSpec.BooleanValue enabled;
            public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
            public final ForgeConfigSpec.ConfigValue<Integer> x;
            public final ForgeConfigSpec.ConfigValue<Integer> y;

            XpLevelTextSetting(ForgeConfigSpec.Builder builder) {
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
    }

    public static class CustomBossbar {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> maxVisible;
        public final ForgeConfigSpec.ConfigValue<Integer> width;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        CustomBossbar(ForgeConfigSpec.Builder builder) {
            builder.comment("Custom Bossbar")
                    .push("custom_bossbar");

            enabled = builder
                    .comment("Toggle whether to use Souls-like bossbars.")
                    .define("enabled", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_CENTER
            );

            maxVisible = builder.define(
                    "max_visible",
                    4
            );

            width = builder.define("width", 320);
            x = builder.define("x", 0);
            y = builder.define("y", 72);

            builder.pop();
        }
    }

    public static class OxygenBar {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> width;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        OxygenBar(ForgeConfigSpec.Builder builder) {
            builder.comment("Oxygen Bar")
                    .push("oxygen_bar");

            enabled = builder
                    .comment("Toggle oxygen bar")
                    .define("enabled", true);

            anchor = builder.defineEnum(
                    "anchor",
                    ElementAnchor.BOTTOM_CENTER
            );

            width = builder.define("width", 182);
            x = builder.define("x", 0);
            y = builder.define("y", 60);

            builder.pop();
        }
    }

    public static class Visibility {
        public final MinecraftGuiSetting minecraftGui;
        public final EpicFightGuiSetting epicfightGui;


        Visibility(ForgeConfigSpec.Builder builder) {
            builder.comment("Visibility").push("visibility");

            minecraftGui = new MinecraftGuiSetting(builder);
            epicfightGui = new EpicFightGuiSetting(builder);

            builder.pop();
        }

        public static class EpicFightGuiSetting {
            public final ForgeConfigSpec.BooleanValue hideStaminaBar;

            EpicFightGuiSetting(ForgeConfigSpec.Builder builder) {
                builder.push("epicfight");

                hideStaminaBar = builder
                        .define("hide_stamina_bar", true);

                builder.pop();
            }
        }

        public static class MinecraftGuiSetting {
            public final MinecraftHotbarSetting hotbar;
            public final ForgeConfigSpec.BooleanValue disableShiftTextOnGamemode;
            public final MinecraftItemNameSetting itemName;
            public final MinecraftRecordOverlay recordOverlay;
            public final ForgeConfigSpec.BooleanValue hideBossbar;
            public final ForgeConfigSpec.BooleanValue hidePotionIcons;

            MinecraftGuiSetting(ForgeConfigSpec.Builder builder) {
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
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                MinecraftItemNameSetting(ForgeConfigSpec.Builder builder) {
                    builder.comment("Vanilla Highlight Item Name Settings").push("item_name");

                    x = builder.define("x", 0);
                    y = builder.define("y", -22);

                    builder.pop();
                }
            }

            public static class MinecraftRecordOverlay {
                public final ForgeConfigSpec.ConfigValue<Integer> x;
                public final ForgeConfigSpec.ConfigValue<Integer> y;

                MinecraftRecordOverlay(ForgeConfigSpec.Builder builder) {
                    builder.comment("Vanilla Action Bar Settings").push("record_overlay");

                    x = builder.define("x", 0);
                    y = builder.define("y", -20);

                    builder.pop();
                }
            }

            public static class MinecraftHotbarSetting {
                public final ForgeConfigSpec.BooleanValue hideHealthLevel;
                public final ForgeConfigSpec.BooleanValue hideArmorLevel;
                public final ForgeConfigSpec.BooleanValue hideHungerLevel;
                public final ForgeConfigSpec.BooleanValue hideAirLevel;
                public final ForgeConfigSpec.BooleanValue hideExperienceBar;
                public final ForgeConfigSpec.BooleanValue hideOffhandSlot;
                public final ForgeConfigSpec.BooleanValue hideMoreOffhandSlots;

                MinecraftHotbarSetting(ForgeConfigSpec.Builder builder) {
                    builder.comment("Vanilla Hotbar settings").push("hotbar");

                    hideHealthLevel = builder.define("hide_health_level", true);
                    hideArmorLevel = builder.define("hide_armor_level", true);
                    hideHungerLevel = builder.define("hide_hunger_level", true);
                    hideAirLevel = builder.define("hide_air_level", true);
                    hideExperienceBar = builder.define("hide_experience_bar", true);
                    hideOffhandSlot = builder.define("hide_offhand_slot", true);
                    hideMoreOffhandSlots = builder
                            .comment("Hides the More Offhand Slots slots in the hotbar.")
                            .define("hide_moreoffhandslots", true);

                    builder.pop();
                }
            }
        }
    }
}