package dev.redshift.client.core;

import dev.redshift.client.modules.ToggleSprintModule;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private static final List<ClientModule> MODULES = new ArrayList<>();
    private static final Map<String, ClientModule> BY_ID = new LinkedHashMap<>();
    private static boolean initialized;

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
                "world_clock",
                "World Clock",
                "Displays the current in-game day time.",
                ModuleCategory.WORLD
        ) {});

        Map<String, Boolean> saved = ClientConfig.load();
        for (ClientModule module : MODULES) {
            module.setEnabled(saved.getOrDefault(module.id(), false));
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
    }

    public static void save() {
        Map<String, Boolean> state = new LinkedHashMap<>();
        for (ClientModule module : MODULES) {
            state.put(module.id(), module.isEnabled());
        }
        ClientConfig.save(state);
    }
}
