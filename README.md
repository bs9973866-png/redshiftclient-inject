# REDSHIFT CLIENT

**A real in-game Fabric client for Minecraft Java 1.21.11.**

This repository has been converted from the temporary Rust desktop-overlay experiment into a client-side Minecraft mod. The ClickGUI is a native Minecraft screen and the HUD is rendered through Fabric's HUD element API.

## Current foundation

- Minecraft Java 1.21.11
- Fabric Loader 0.20.1
- Fabric API 0.141.1+1.21.11
- Java 21 bytecode and official Mojang mappings (build runtime: JDK 25 for current Loom)
- Black / crimson in-game ClickGUI with native Minecraft screen input
- Right Shift opens and closes the GUI by default; the GUI key is configurable
- Categories: Combat, Visuals, Movement, HUD and World, plus a Settings page
- Search modules by name, description or ID within the selected category
- Left-click toggles a module; middle-click opens module options; right-click assigns an individual module keybind
- Module states, boolean module settings, module keybinds and GUI keybind are saved to `config/redshift-client.json`
- HUD modules render inside the actual Minecraft HUD
- Modules: Sprint Assist, Watermark, Coordinates, FPS Counter, Keystrokes, Armor Status, Held Item, Attack Cooldown, Crimson Crosshair and World Clock

## Build

Build requirement: JDK 25 and Gradle 9.9.1 (required by the current Fabric Loom build plugin). The mod is compiled with Java 21 bytecode and runs on the Java 21 runtime used by Minecraft 1.21.11.

Run: gradle build --no-daemon

The remapped mod JAR is written to:

build/libs/redshift-client-0.1.0.jar

Install it by placing the JAR into the mods folder of a Minecraft 1.21.11 Fabric instance with Fabric API installed.

## Controls

- Right Shift opens / closes REDSHIFT ClickGUI.
- Click a category on the left to switch module groups.
- Left-click a module card to enable or disable it.
- Right-click a module card to assign a keybind; press Escape to cancel.
- Middle-click a module card to open its options.
- Sprint Assist includes a saved option to pause while using an item.
- Use the search field above the cards to filter the selected category.
- Open Settings to change the ClickGUI hotkey.

## Project direction

The next development milestones are a draggable HUD editor, slider settings, theme customization, additional visual modules, and more client-side quality-of-life options. This is a Fabric mod, not an external overlay. It does not inject code into a process or bypass anti-cheat.
