package net.tablesouls.souls_combat_hud.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.souls_combat_hud";

    public static KeyMapping CYCLE_CONSUMABLE;
    public static KeyMapping CYCLE_SPELL;
    public static KeyMapping USE_CONSUMABLE;
    public static KeyMapping CYCLE_WEAPON;
    public static KeyMapping CYCLE_OFFHAND;
    public static KeyMapping OPEN_PARTY_MENU;

    public static void register(RegisterKeyMappingsEvent event) {
        CYCLE_CONSUMABLE = new KeyMapping(
                "key.souls_combat_hud.cycle_consumable",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                CATEGORY
        );
        event.register(CYCLE_CONSUMABLE);

        CYCLE_SPELL = new KeyMapping(
                "key.souls_combat_hud.cycle_spell",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                CATEGORY
        );
        event.register(CYCLE_SPELL);

        USE_CONSUMABLE = new KeyMapping(
                "key.souls_combat_hud.use_consumable",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY
        );
        event.register(USE_CONSUMABLE);

        CYCLE_WEAPON = new KeyMapping(
                "key.souls_combat_hud.cycle_weapon",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT,
                CATEGORY
        );
        event.register(CYCLE_WEAPON);

        CYCLE_OFFHAND = new KeyMapping(
                "key.souls_combat_hud.cycle_offhand",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT,
                CATEGORY
        );
        event.register(CYCLE_OFFHAND);

        OPEN_PARTY_MENU = new KeyMapping(
                "key.souls_combat_hud.open_party_menu",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                CATEGORY
        );
        event.register(OPEN_PARTY_MENU);
    }
}