package dev.redshift.client.core;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ClientModule {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private boolean enabled;
    private int keybind = GLFW.GLFW_KEY_UNKNOWN;
    private final List<BooleanSetting> booleanSettings = new ArrayList<>();

    protected ClientModule(String id, String name, String description, ModuleCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public final String id() {
        return id;
    }

    public final String name() {
        return name;
    }

    public final String description() {
        return description;
    }

    public final ModuleCategory category() {
        return category;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final int keybind() {
        return keybind;
    }

    public final void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    protected final BooleanSetting addBooleanSetting(
            String id,
            String name,
            String description,
            boolean defaultValue
    ) {
        BooleanSetting setting = new BooleanSetting(id, name, description, defaultValue);
        booleanSettings.add(setting);
        return setting;
    }

    public final List<BooleanSetting> booleanSettings() {
        return Collections.unmodifiableList(booleanSettings);
    }

    public final BooleanSetting booleanSetting(String id) {
        return booleanSettings.stream()
                .filter(setting -> setting.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(Minecraft client) {
    }
}
