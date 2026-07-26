package net.tablesouls.souls_combat_hud.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.tablesouls.souls_combat_hud.util.ElementAnchor;

import java.util.List;

public final class SoulsCombatHUDConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final SkillOverlay SKILL_OVERLAY;
    public static final EquipmentHud EQUIPMENT_HUD;
    public static final CustomBossbar CUSTOM_BOSSBAR;
    public static final Visibility VISIBILITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Client Config");

        SKILL_OVERLAY = new SkillOverlay(builder);
        EQUIPMENT_HUD = new EquipmentHud(builder);
        CUSTOM_BOSSBAR = new CustomBossbar(builder);
        VISIBILITY = new Visibility(builder);

        builder.pop();

        CLIENT_SPEC = builder.build();
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
            builder.comment("Skill Overlay")
                    .push("skill_overlay");

            enabled = builder
                    .comment("Toggle whether to use custom skill overlay for Epic Fight.")
                    .define("skill_overlay", true);

            anchor = builder.defineEnum(
                    "skill_overlay_anchor",
                    ElementAnchor.BOTTOM_LEFT
            );

            x = builder.define(
                    "skill_overlay_x",
                    12
            );

            y = builder.define(
                    "skill_overlay_y",
                    120
            );

            builder.pop();
        }
    }

    public static class EquipmentHud {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue cycleSound;
        public final ForgeConfigSpec.BooleanValue cycleConsumableSwitch;

        public final Slots slots;

        public final ForgeConfigSpec.EnumValue<ElementAnchor> anchor;
        public final ForgeConfigSpec.ConfigValue<Integer> x;
        public final ForgeConfigSpec.ConfigValue<Integer> y;

        EquipmentHud(ForgeConfigSpec.Builder builder) {
            builder.comment("Equipment Hud")
                    .push("equipment_hud");

            enabled = builder
                    .comment("Toggle whether to use the custom equipment HUD.")
                    .define("equipment_hud", true);

            cycleSound = builder
                    .comment("Should cycling items play a sound.")
                    .define("cycle_sound", true);

            cycleConsumableSwitch = builder
                    .comment("Should cycling your consumables jump to the item.")
                    .define("cycle_consumable_switch", false);

            slots = new Slots(builder);

            anchor = builder.defineEnum(
                    "equipment_hud_anchor",
                    ElementAnchor.BOTTOM_LEFT
            );

            x = builder.define(
                    "equipment_hud_x",
                    60
            );

            y = builder.define(
                    "equipment_hud_y",
                    60
            );

            builder.pop();
        }

        public static class Slots {
            public final WeaponSlot weapon;
            public final OffhandSlot offhand;
            public final ConsumableSlot consumable;
            public final SpellSlot spell;

            Slots(ForgeConfigSpec.Builder builder) {
                builder.comment("Slots")
                        .push("equipment_slots");

                weapon = new WeaponSlot(builder);
                offhand = new OffhandSlot(builder);
                consumable = new ConsumableSlot(builder);
                spell = new SpellSlot(builder);

                builder.pop();
            }

            public static class WeaponSlot {
                public final ForgeConfigSpec.BooleanValue enabled;
                public final ForgeConfigSpec.BooleanValue include_combat_preferred;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> include_weapons_list;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> exclude_weapons_list;

                WeaponSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("weapon_slot");

                    enabled = builder.define("enabled", true);
                    include_combat_preferred = builder
                            .comment("Include Combat Preferred Items (Epic Fight)")
                            .define("include_combat_prefered", true);
                    include_weapons_list = builder
                            .comment("Include items as a weapon")
                            .defineList("include_weapon_list", List.of(), o -> o instanceof String);
                    exclude_weapons_list = builder
                            .comment("Exclude items as a weapon")
                            .defineList("exclude_weapon_list", List.of(), o -> o instanceof String);
                    builder.pop();
                }
            }

            public static class OffhandSlot {
                public final ForgeConfigSpec.BooleanValue enabled;

                OffhandSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("offhand_slot");

                    enabled = builder.define("enabled", true);

                    builder.pop();
                }
            }

            public static class ConsumableSlot {
                public final ForgeConfigSpec.BooleanValue enabled;
                public final Toggle name;
                public final ForgeConfigSpec.IntValue maxPreviewSlots;

                ConsumableSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("consumable_slot");

                    enabled = builder.define("enabled", true);
                    name = new Toggle(builder, "name", true);
                    maxPreviewSlots = builder.defineInRange("max_preview_slots", 3, 0, 12);

                    builder.pop();
                }
            }

            public static class SpellSlot {
                public final ForgeConfigSpec.BooleanValue enabled;
                public final Toggle name;
                public final ForgeConfigSpec.IntValue maxPreviewSlots;

                SpellSlot(ForgeConfigSpec.Builder builder) {
                    builder.push("spell_slot");

                    enabled = builder.define("enabled", true);
                    name = new Toggle(builder, "name", true);
                    maxPreviewSlots = builder.defineInRange("max_preview_slots", 3, 0, 12);

                    builder.pop();
                }
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
                    .define("custom_bossbar", true);

            anchor = builder.defineEnum(
                    "custom_bossbar_anchor",
                    ElementAnchor.BOTTOM_CENTER
            );

            maxVisible = builder.define(
                    "custom_bossbar_max_visible",
                    4
            );

            width = builder.define(
                    "custom_bossbar_width",
                    320
            );

            x = builder.define(
                    "custom_bossbar_x",
                    0
            );

            y = builder.define(
                    "custom_bossbar_y",
                    60
            );

            builder.pop();
        }
    }

    public static class Visibility {
        public final ForgeConfigSpec.BooleanValue hideOffhandSlot;
        public final ForgeConfigSpec.BooleanValue hideMoreOffhandSlots;
        public final ForgeConfigSpec.BooleanValue hideBossbar;

        Visibility(ForgeConfigSpec.Builder builder) {
            builder.comment("Visibility")
                    .push("visibility");

            hideOffhandSlot = builder
                    .comment("Hides the offhand slot in the hotbar.")
                    .define("hide_offhand_slot", true);

            hideMoreOffhandSlots = builder
                    .comment("Hides the More Offhand Slots slots in the hotbar.")
                    .define("hide_moreoffhandslots", true);

            hideBossbar = builder
                    .comment("Hides the vanilla bossbar.")
                    .define("hide_bossbar", true);

            builder.pop();
        }
    }
}