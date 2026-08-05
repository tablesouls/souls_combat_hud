package net.tablesouls.souls_combat_hud.client.render.status_gauge;

import java.util.List;

public enum CrestAttribute {
    PRIVATE("private"),
    HUNGER("hunger"),
    ARMOR("armor"),
    THIRST("thirst");

    public static final List<CrestAttribute> DEFAULT_ORDER = List.of(PRIVATE, HUNGER, ARMOR, THIRST);

    private final String key;

    CrestAttribute(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static CrestAttribute byKey(String key) {
        for (CrestAttribute attribute : values()) {
            if (attribute.key.equals(key)) return attribute;
        }
        return null;
    }
}