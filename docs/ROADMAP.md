# REDSHIFT CLIENT roadmap

## 0.1 — Real in-game Fabric foundation
- [x] Replace the temporary Rust overlay with a Fabric mod project.
- [x] Target Minecraft 1.21.11 and Java 21.
- [x] Native in-game ClickGUI opened by Right Shift.
- [x] Category navigation: Combat, Visuals, Movement, HUD, World.
- [x] Module enable / disable framework.
- [x] Persist module states to a local JSON config.
- [x] Render client HUD modules inside Minecraft.
- [x] Add Sprint Assist, Watermark, Coordinates, FPS Counter, Attack Cooldown, Crimson Crosshair and World Clock.
- [ ] Verify the remapped JAR in a real 1.21.11 Fabric game instance.

## 0.2 — ClickGUI and settings
- [ ] Animated transitions and refined card layout.
- [ ] Search and module filtering.
- [ ] Per-module settings and sliders.
- [ ] Configurable keybinds.
- [ ] Multiple named profiles.

## 0.3 — HUD editor
- [ ] Drag-and-drop HUD element positioning.
- [ ] Independent HUD scale and opacity.
- [ ] Toggleable HUD elements and live preview.
- [ ] Custom fonts and color presets.

## 0.4 — Visual and quality-of-life modules
- [ ] More crosshair styles and hit feedback visuals.
- [ ] Item / armor status HUD.
- [ ] Keystrokes and CPS display.
- [ ] World and environment display options.
- [ ] Additional movement quality-of-life modules.

## Compatibility boundary

REDSHIFT is a normal client-side Fabric mod. Its features run through Minecraft and Fabric APIs; it does not inject native code into the game process or bypass anti-cheat systems.
