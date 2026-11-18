package dev.redshift.client.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HudLayout {
    public record Widget(
            String id,
            String label,
            int width,
            int height,
            float defaultX,
            float defaultY
    ) {
    }

    private record Position(float x, float y) {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("redshift-hud-layout.json");

    private static final List<Widget> WIDGETS = List.of(
            new Widget("watermark", "Watermark", 124, 25, 0.015F, 0.02F),
            new Widget("fps_counter", "FPS Counter", 122, 20, 0.015F, 0.085F),
            new Widget("coordinates", "Coordinates", 150, 20, 0.015F, 0.14F),
            new Widget("held_item", "Held Item", 170, 30, 0.015F, 0.82F),
            new Widget("armor_status", "Armor Status", 100, 26, 0.44F, 0.90F),
            new Widget("keystrokes", "Keystrokes", 78, 68, 0.89F, 0.76F),
            new Widget("world_clock", "World Clock", 96, 20, 0.86F, 0.02F),
            new Widget("attack_cooldown", "Attack Cooldown", 72, 8, 0.458F, 0.54F),
            new Widget("crimson_crosshair", "Crimson Crosshair", 16, 16, 0.49F, 0.48F)
    );

    private static final Map<String, Position> POSITIONS = new LinkedHashMap<>();

    private HudLayout() {
    }

    public static List<Widget> widgets() {
        return WIDGETS;
    }

    public static void load() {
        POSITIONS.clear();
        if (!Files.exists(PATH)) {
            return;
        }

        try {
            JsonObject root = GSON.fromJson(Files.readString(PATH), JsonObject.class);
            if (root == null) {
                return;
            }

            for (Widget widget : WIDGETS) {
                JsonElement value = root.get(widget.id());
                if (value == null || !value.isJsonObject()) {
                    continue;
                }

                JsonObject position = value.getAsJsonObject();
                if (position.has("x") && position.has("y")
                        && position.get("x").isJsonPrimitive()
                        && position.get("y").isJsonPrimitive()) {
                    float x = position.get("x").getAsFloat();
                    float y = position.get("y").getAsFloat();
                    if (Float.isFinite(x) && Float.isFinite(y)) {
                        POSITIONS.put(widget.id(), new Position(
                                clamp(x, 0.0F, 1.0F),
                                clamp(y, 0.0F, 1.0F)
                        ));
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            System.err.println("[REDSHIFT] Could not read HUD layout: " + exception.getMessage());
        }
    }

    public static int x(String id, int screenWidth) {
        Widget widget = find(id);
        Position position = POSITIONS.get(id);
        float fraction = position == null ? widget.defaultX() : position.x();
        return clamp(Math.round(fraction * screenWidth), 0, Math.max(0, screenWidth - widget.width()));
    }

    public static int y(String id, int screenHeight) {
        Widget widget = find(id);
        Position position = POSITIONS.get(id);
        float fraction = position == null ? widget.defaultY() : position.y();
        return clamp(Math.round(fraction * screenHeight), 0, Math.max(0, screenHeight - widget.height()));
    }

    public static void move(String id, int x, int y, int screenWidth, int screenHeight) {
        Widget widget = find(id);
        int maxX = Math.max(0, screenWidth - widget.width());
        int maxY = Math.max(0, screenHeight - widget.height());
        float normalizedX = maxX == 0 ? 0.0F : (float) clamp(x, 0, maxX) / screenWidth;
        float normalizedY = maxY == 0 ? 0.0F : (float) clamp(y, 0, maxY) / screenHeight;
        POSITIONS.put(id, new Position(normalizedX, normalizedY));
    }

    public static void reset() {
        POSITIONS.clear();
        save();
    }

    public static void save() {
        JsonObject root = new JsonObject();
        POSITIONS.forEach((id, position) -> {
            JsonObject value = new JsonObject();
            value.addProperty("x", position.x());
            value.addProperty("y", position.y());
            root.add(id, value);
        });

        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(root));
        } catch (IOException exception) {
            System.err.println("[REDSHIFT] Could not save HUD layout: " + exception.getMessage());
        }
    }

    private static Widget find(String id) {
        return WIDGETS.stream()
                .filter(widget -> widget.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown HUD widget: " + id));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
