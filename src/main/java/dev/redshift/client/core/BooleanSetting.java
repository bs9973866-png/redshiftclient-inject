package dev.redshift.client.core;

public final class BooleanSetting {
    private final String id;
    private final String name;
    private final String description;
    private boolean enabled;

    public BooleanSetting(String id, String name, String description, boolean defaultValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = defaultValue;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}
