package net.tablesouls.souls_combat_hud.party;

import java.util.List;

public enum PartyStatType {
    HEALTH(0.0f),
    MAX_HEALTH(20.0f),
    STAMINA(0.0f),
    MAX_STAMINA(0.0f),
    MANA(0.0f),
    MAX_MANA(0.0f),
    DIMENSION(""),
    STATUS_EFFECTS(List.<PartyEffectSnapshot>of());

    private final Object empty;

    PartyStatType(Object empty) {
        this.empty = empty;
    }

    public Object empty() {
        return empty;
    }
}