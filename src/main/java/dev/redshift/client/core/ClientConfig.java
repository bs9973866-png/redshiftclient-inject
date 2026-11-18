package dev.redshift.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("redshift-client.json");

    private static int cachedGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static boolean guiKeyLoaded;

    private ClientConfig() {
    }

    private static JsonObject readRoot() {
        if (!Files.exists(CONFIG_PATH)) {
            return new JsonObject();
        }

        try {
            JsonObject root = GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class);
            return root == null ? new JsonObject() : root;
        } catch (IOException | RuntimeException exception) {
            System.err.println("[REDSHIFT] Could not read config: " + exception.getMessage());
            return new JsonObject();
        }
    }

    private static void writeRoot(JsonObject root) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException exception) {
            System.err.println("[REDSHIFT] Could not save config: " + exception.getMessage());
        }
    }

    public static Map<String, Boolean> load() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        JsonObject root = readRoot();
        if (!root.has("modules") || !root.get("modules").isJsonObject()) {
            return result;
        }

        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("modules").entrySet()) {
            if (entry.getValue().isJsonPrimitive()
                    && entry.getValue().getAsJsonPrimitive().isBoolean()) {
                result.put(entry.getKey(), entry.getValue().getAsBoolean());
            }
        }

        return result;
    }

    public static Map<String, Integer> loadModuleKeybinds() {
        Map<String, Integer> result = new LinkedHashMap<>();
        JsonObject root = readRoot();
        if (!root.has("moduleKeybinds") || !root.get("moduleKeybinds").isJsonObject()) {
            return result;
        }

        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("moduleKeybinds").entrySet()) {
            if (entry.getValue().isJsonPrimitive()
                    && entry.getValue().getAsJsonPrimitive().isNumber()) {
                result.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
        return result;
    }

    public static Map<String, Map<String, Boolean>> loadModuleSettings() {
        Map<String, Map<String, Boolean>> result = new LinkedHashMap<>();
        JsonObject root = readRoot();
        if (!root.has("moduleSettings") || !root.get("moduleSettings").isJsonObject()) {
            return result;
        }

        JsonObject modules = root.getAsJsonObject("moduleSettings");
        for (Map.Entry<String, JsonElement> moduleEntry : modules.entrySet()) {
            if (!moduleEntry.getValue().isJsonObject()) {
                continue;
            }

            Map<String, Boolean> settings = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> settingEntry
                    : moduleEntry.getValue().getAsJsonObject().entrySet()) {
                JsonElement value = settingEntry.getValue();
                if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                    settings.put(settingEntry.getKey(), value.getAsBoolean());
                }
            }
            result.put(moduleEntry.getKey(), settings);
        }
        return result;
    }

    public static void save(
            Map<String, Boolean> enabledModules,
            Map<String, Integer> moduleKeybinds,
            Map<String, Map<String, Boolean>> moduleSettings
    ) {
        JsonObject root = readRoot();
        JsonObject modules = new JsonObject();
        enabledModules.forEach(modules::addProperty);
        root.add("modules", modules);

        JsonObject keybinds = new JsonObject();
        moduleKeybinds.forEach(keybinds::addProperty);
        root.add("moduleKeybinds", keybinds);

        JsonObject settingsRoot = new JsonObject();
        moduleSettings.forEach((moduleId, settings) -> {
            JsonObject settingsObject = new JsonObject();
            settings.forEach(settingsObject::addProperty);
            settingsRoot.add(moduleId, settingsObject);
        });
        root.add("moduleSettings", settingsRoot);
        writeRoot(root);
    }

    public static synchronized int guiKey() {
        if (guiKeyLoaded) {
            return cachedGuiKey;
        }

        JsonObject root = readRoot();
        if (root.has("guiKey") && root.get("guiKey").isJsonPrimitive()
                && root.get("guiKey").getAsJsonPrimitive().isNumber()) {
            cachedGuiKey = root.get("guiKey").getAsInt();
        }

        if (cachedGuiKey < 0) {
            cachedGuiKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        }
        guiKeyLoaded = true;
        return cachedGuiKey;
    }

    public static synchronized void setGuiKey(int keyCode) {
        if (keyCode < 0) {
            return;
        }

        cachedGuiKey = keyCode;
        guiKeyLoaded = true;
        JsonObject root = readRoot();
        root.addProperty("guiKey", keyCode);
        writeRoot(root);
    }
}
