package net.tablesouls.souls_combat_hud.util;

import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BossEventLookup {
    private static final Map<Class<?>, Field> CACHE = new ConcurrentHashMap<>();
    private static final Field NONE = sentinel();

    public static BossEvent find(LivingEntity entity) {
        Field field = CACHE.computeIfAbsent(entity.getClass(), BossEventLookup::locate);
        if (field == NONE) return null;

        try {
            Object value = field.get(entity);
            return value instanceof BossEvent be ? be : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static Field locate(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (BossEvent.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        return NONE;
    }

    private static Field sentinel() {
        try {
            return BossEventLookup.class.getDeclaredField("NONE_PLACEHOLDER");
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    @SuppressWarnings("unused")
    private static Field NONE_PLACEHOLDER;
}