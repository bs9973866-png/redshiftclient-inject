# REDSHIFT roadmap

## Milestone 0.1 — project foundation
- [x] Empty repository initialized
- [x] Fabric Loom project for Minecraft 1.21.11
- [x] Java 21 client entrypoint
- [x] Right Shift menu binding
- [x] First Black / Crimson Liquid Glass-style ClickGUI shell
- [x] English and Russian key names
- [ ] Verify Gradle build in CI and fix any API/mapping issues

## Milestone 0.2 — usable ClickGUI
- [ ] Working category navigation
- [ ] Module registry with enabled state and keybinds
- [ ] Search and favorites
- [ ] Persistent JSON configuration
- [ ] UI scale and accent color settings

## Milestone 0.3 — visual system
- [ ] Animated transitions with reduced-motion option
- [ ] Glass material controls (opacity, highlights, tint)
- [ ] HUD editor and draggable widgets
- [ ] Theme presets

## Milestone 0.4 — Windows companion
- [ ] Separate Windows desktop companion
- [ ] Detect Minecraft process without modifying it
- [ ] Manage profiles and config files
- [ ] Safe launch/install workflow for the Fabric component

## Architecture and safety
The Minecraft integration is a normal Fabric client mod. The companion app may detect the running game and manage configuration, but will not inject native DLLs, patch process memory, evade anti-cheat, or conceal itself.
