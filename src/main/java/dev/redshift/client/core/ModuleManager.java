package dev.redshift.client.core;

import dev.redshift.client.modules.ToggleSprintModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final List<ClientModule> MODULES = new ArrayList<>();
    private static final Map<String, ClientModule> BY_ID = new LinkedHashMap<>();
    private static boolean initialized;
    private static final Map<String, Boolean> KEY_WAS_DOWN = new LinkedHashMap<>();

    private ModuleManager() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        register(new ClientModule(
                "attack_cooldown",
                "Attack Cooldown",
                "Shows the vanilla attack recharge indicator.",
                ModuleCategory.COMBAT
        ) {});

        register(new ClientModule(
                "crimson_crosshair",
                "Crimson Crosshair",
                "Draws a compact red reticle over the vanilla crosshair.",
                ModuleCategory.VISUALS
        ) {});

        register(new ToggleSprintModule());

        register(new ClientModule(
                "watermark",
                "Watermark",
                "Displays the REDSHIFT client tag in-game.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "coordinates",
                "Coordinates",
                "Displays your current XYZ coordinates.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "fps_counter",
                "FPS Counter",
                "Displays the live Minecraft frame rate.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "keystrokes",
                "Keystrokes",
                "Displays live WASD, jump and sneak key states.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "armor_status",
                "Armor Status",
                "Displays equipped armor and remaining durability.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "held_item",
                "Held Item",
                "Displays your selected item and its durability.",
                ModuleCategory.HUD
        ) {});

        register(new ClientModule(
                "world_clock",
                "World Clock",
                "Displays the current in-game day time.",
                ModuleCategory.WORLD
        ) {});

        Map<String, Boolean> saved = ClientConfig.load();
        Map<String, Integer> savedKeybinds = ClientConfig.loadModuleKeybinds();
        Map<String, Map<String, Boolean>> savedSettings = ClientConfig.loadModuleSettings();
        for (ClientModule module : MODULES) {
            module.setEnabled(saved.getOrDefault(module.id(), false));
            module.setKeybind(savedKeybinds.getOrDefault(module.id(), GLFW.GLFW_KEY_UNKNOWN));

            Map<String, Boolean> moduleSettings = savedSettings.getOrDefault(module.id(), Map.of());
            for (BooleanSetting setting : module.booleanSettings()) {
                setting.setEnabled(moduleSettings.getOrDefault(setting.id(), setting.isEnabled()));
            }
        }

        initialized = true;
    }

    private static void register(ClientModule module) {
        MODULES.add(module);
        BY_ID.put(module.id(), module);
    }

    public static List<ClientModule> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<ClientModule> byCategory(ModuleCategory category) {
        return MODULES.stream()
                .filter(module -> module.category() == category)
                .toList();
    }

    public static ClientModule get(String id) {
        return BY_ID.get(id);
    }

    public static boolean isEnabled(String id) {
        ClientModule module = BY_ID.get(id);
        return module != null && module.isEnabled();
    }

    public static void tick(Minecraft client) {
        if (!initialized) {
            return;
        }

        for (ClientModule module : MODULES) {
            if (module.isEnabled()) {
                module.onTick(client);
            }
        }

        if (client.screen == null) {
            long window = client.getWindow().handle();
            boolean changed = false;
            for (ClientModule module : MODULES) {
                int key = module.keybind();
                if (key == GLFW.GLFW_KEY_UNKNOWN) {
                    KEY_WAS_DOWN.put(module.id(), false);
                    continue;
                }

                boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
                boolean wasDown = KEY_WAS_DOWN.getOrDefault(module.id(), false);
                if (down && !wasDown) {
                    module.toggle();
                    changed = true;
                }
                KEY_WAS_DOWN.put(module.id(), down);
            }
            if (changed) {
                save();
            }
        } else {
            KEY_WAS_DOWN.replaceAll((id, wasDown) -> false);
        }
    }

    public static void save() {
        Map<String, Boolean> state = new LinkedHashMap<>();
        Map<String, Integer> keybinds = new LinkedHashMap<>();
        Map<String, Map<String, Boolean>> settings = new LinkedHashMap<>();

        for (ClientModule module : MODULES) {
            state.put(module.id(), module.isEnabled());
            if (module.keybind() != GLFW.GLFW_KEY_UNKNOWN) {
                keybinds.put(module.id(), module.keybind());
            }

            Map<String, Boolean> moduleSettings = new LinkedHashMap<>();
            for (BooleanSetting setting : module.booleanSettings()) {
                moduleSettings.put(setting.id(), setting.isEnabled());
            }
            if (!moduleSettings.isEmpty()) {
                settings.put(module.id(), moduleSettings);
            }
        }
        ClientConfig.save(state, keybinds, settings);
    }
}
