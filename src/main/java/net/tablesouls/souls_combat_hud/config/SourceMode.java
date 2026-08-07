package net.tablesouls.souls_combat_hud.config;

public interface SourceMode {
    default boolean isAuto() {
        return ((Enum<?>) this).ordinal() == 0;
    }
}