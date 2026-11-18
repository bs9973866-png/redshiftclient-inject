package dev.redshift.client.core;

public enum ModuleCategory {
    COMBAT("Combat"),
    VISUALS("Visuals"),
    MOVEMENT("Movement"),
    HUD("HUD"),
    WORLD("World");

    private final String title;

    ModuleCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
