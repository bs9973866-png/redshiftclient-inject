# REDSHIFT CLIENT roadmap

## 0.1 — Real in-game Fabric foundation
- [x] Replace the temporary Rust overlay with a Fabric mod project.
- [x] Target Minecraft 1.21.11 and Java 21.
- [x] Native in-game ClickGUI opened by Right Shift.
- [x] Category navigation: Combat, Visuals, Movement, HUD, World.
- [x] Module enable / disable framework.
- [x] Persist module states to a local JSON config.
- [x] Render client HUD modules inside Minecraft.
- [x] Add Sprint Assist, Watermark, Coordinates, FPS Counter, Attack Cooldown, Crimson Crosshair, World Clock, Keystrokes, Armor Status and Held Item.
- [ ] Verify the remapped JAR in a real 1.21.11 Fabric game instance.

## 0.2 — ClickGUI and settings
- [ ] Animated transitions and refined card layout.
- [x] Search and module filtering by module name, description and ID.
- [x] Reusable boolean-setting framework and module options screen.
- [x] Sprint Assist option: pause while using items.
- [ ] Numeric settings and sliders.
- [x] Configurable ClickGUI keybind and individual module keybinds.
- [ ] Multiple named profiles.

## 0.3 — HUD editor
- [x] Drag-and-drop HUD element positioning with persistent normalized coordinates.
- [ ] Independent HUD scale and opacity.
- [x] Toggleable HUD elements and draggable live HUD layout preview.
- [ ] Custom fonts and color presets.

## 0.4 — Visual and quality-of-life modules
- [ ] More crosshair styles and hit feedback visuals.
- [x] Held-item and armor durability HUD.
- [x] Keystrokes display.
- [ ] CPS display.
- [ ] World and environment display options.
- [ ] Additional movement quality-of-life modules.

## Compatibility boundary

REDSHIFT is a normal client-side Fabric mod. Its features run through Minecraft and Fabric APIs; it does not inject native code into the game process or bypass anti-cheat systems.
