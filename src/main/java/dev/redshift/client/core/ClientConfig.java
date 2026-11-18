package dev.redshift.client.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("redshift-client.json");

    private ClientConfig() {
    }

    public static Map<String, Boolean> load() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        if (!Files.exists(CONFIG_PATH)) {
            return result;
        }

        try {
            JsonObject root = GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class);
            if (root == null || !root.has("modules") || !root.get("modules").isJsonObject()) {
                return result;
            }

            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("modules").entrySet()) {
                if (entry.getValue().isJsonPrimitive()
                        && entry.getValue().getAsJsonPrimitive().isBoolean()) {
                    result.put(entry.getKey(), entry.getValue().getAsBoolean());
                }
            }
        } catch (IOException | RuntimeException exception) {
            System.err.println("[REDSHIFT] Could not read config: " + exception.getMessage());
        }

        return result;
    }

    public static void save(Map<String, Boolean> enabledModules) {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();
        enabledModules.forEach(modules::addProperty);
        root.add("modules", modules);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(root));
        } catch (IOException exception) {
            System.err.println("[REDSHIFT] Could not save config: " + exception.getMessage());
        }
    }
}
