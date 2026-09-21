package net.tablesouls.souls_combat_hud.compat.parcool;

import net.minecraft.world.entity.player.Player;
import net.tablesouls.souls_combat_hud.compat.ResourceSource;
import net.tablesouls.souls_combat_hud.config.StaminaSourceMode;

import java.lang.reflect.Method;

public class ParcoolStaminaSource implements ResourceSource<StaminaSourceMode> {
    private interface Bridge {
        boolean isAvailable(Player player);

        double getCurrent(Player player);

        double getMax(Player player);
    }

    private static final Bridge BRIDGE = detectBridge();

    @Override
    public StaminaSourceMode mode() {
        return StaminaSourceMode.PARCOOL;
    }

    @Override
    public boolean isAvailable(Player player) {
        return ParcoolCompat.LOADED && BRIDGE != null && BRIDGE.isAvailable(player);
    }

    @Override
    public float getCurrent(Player player) {
        return BRIDGE != null ? (float) BRIDGE.getCurrent(player) : 0.0f;
    }

    @Override
    public float getMax(Player player) {
        return BRIDGE != null ? (float) BRIDGE.getMax(player) : 0.0f;
    }

    private static Bridge detectBridge() {
        Bridge bridge = newAPI();
        if (bridge != null) {
            return bridge;
        }
        return oldAPI();
    }

    private static Bridge newAPI() {
        try {
            Class<?> parkourabilityClass = Class.forName(
                    "com.alrex.parcool.common.Parkourability");
            Class<?> readableStaminaClass = Class.forName(
                    "com.alrex.parcool.api.stamina.IReadableStamina");

            Method getPlayerParkourability = parkourabilityClass.getMethod("get", Player.class);
            Method getStamina = parkourabilityClass.getMethod("getStamina");
            Method value = readableStaminaClass.getMethod("value");
            Method max = readableStaminaClass.getMethod("max");

            return new Bridge() {
                @Override
                public boolean isAvailable(Player player) {
                    return resolveStamina(player) != null;
                }

                @Override
                public double getCurrent(Player player) {
                    Object stamina = resolveStamina(player);
                    if (stamina == null) return 0.0;
                    try {
                        return (double) value.invoke(stamina);
                    } catch (ReflectiveOperationException e) {
                        return 0.0;
                    }
                }

                @Override
                public double getMax(Player player) {
                    Object stamina = resolveStamina(player);
                    if (stamina == null) return 0.0;
                    try {
                        return (double) max.invoke(stamina);
                    } catch (ReflectiveOperationException e) {
                        return 0.0;
                    }
                }

                private Object resolveStamina(Player player) {
                    try {
                        Object parkourability = getPlayerParkourability.invoke(null, player);
                        if (parkourability == null) return null;
                        return getStamina.invoke(parkourability);
                    } catch (ReflectiveOperationException e) {
                        return null;
                    }
                }
            };
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Bridge oldAPI() {
        try {
            Class<?> staminaClass = Class.forName(
                    "com.alrex.parcool.api.Stamina");

            Method get = staminaClass.getMethod("get", Player.class);
            Method getValue = staminaClass.getMethod("getValue");
            Method getMaxValue = staminaClass.getMethod("getMaxValue");

            return new Bridge() {
                @Override
                public boolean isAvailable(Player player) {
                    return resolveStamina(player) != null;
                }

                @Override
                public double getCurrent(Player player) {
                    Object stamina = resolveStamina(player);
                    if (stamina == null) return 0.0;
                    try {
                        return ((Integer) getValue.invoke(stamina)).doubleValue();
                    } catch (ReflectiveOperationException e) {
                        return 0.0;
                    }
                }

                @Override
                public double getMax(Player player) {
                    Object stamina = resolveStamina(player);
                    if (stamina == null) return 0.0;
                    try {
                        int maxValue = (Integer) getMaxValue.invoke(stamina);
                        return maxValue > 0 ? maxValue : getCurrent(player);
                    } catch (ReflectiveOperationException e) {
                        return 0.0;
                    }
                }

                private Object resolveStamina(Player player) {
                    try {
                        return get.invoke(null, player);
                    } catch (ReflectiveOperationException e) {
                        return null;
                    }
                }
            };
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}