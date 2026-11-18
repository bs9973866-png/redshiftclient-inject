# REDSHIFT INJECT CLIENT

Standalone Windows overlay for Minecraft Java 1.21.11.

REDSHIFT is a separate desktop application. It does not require Fabric, Forge, NeoForge, Quilt, or any other Minecraft mod loader.

## Current foundation

- Rust desktop application
- Transparent, borderless, always-on-top window
- Black / Crimson / Liquid Glass visual direction
- Global Right Shift menu toggle
- Minecraft window-title detection, bounds, and foreground state
- Optional follow mode that positions the panel near the focused Minecraft window
- Optional automatic menu hiding when Minecraft loses focus, with Right Shift manual override
- Optional HUD-widget visibility limited to the focused Minecraft window
- Dashboard, HUD Editor, Visuals, Profiles, and Settings sections
- Draggable status, FPS-preview, and crosshair-preview widgets
- Persistent local JSON profile for widget positions and visual settings
- Save and load a local JSON profile under `%APPDATA%/REDSHIFT/profiles/`
- Windows release build through GitHub Actions

The overlay is independent of the Minecraft mod loader and can sit above Vanilla, Fabric, Forge, NeoForge, or Quilt game windows. This is currently an external UI foundation: it does not read or modify Minecraft memory, inject DLLs, bypass anti-cheat, or hide from screen capture. Game telemetry and in-game rendering are not implemented yet.

## Build

Requirements: Rust stable, Windows 10/11, and Visual Studio C++ Build Tools (MSVC).

Run: cargo build --release

Executable: target/release/redshift-inject-client.exe

## Controls

- Right Shift — toggle the REDSHIFT menu
- Tabs — Dashboard, HUD, Visuals, Profiles, Settings

See docs/ROADMAP.md for planned milestones.


### Interface scaling

The Visual System tab includes a persistent interface scale control (0.8×–1.3×). The selected scale is saved in the local JSON profile and applied to the entire desktop overlay window.
