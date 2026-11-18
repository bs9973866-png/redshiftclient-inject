# REDSHIFT roadmap

## 0.1 — Standalone overlay foundation
- [x] Replace Fabric project structure with Rust desktop application
- [x] Transparent always-on-top window
- [x] Right Shift global hotkey
- [x] Basic Minecraft window-title detection
- [x] Black / Crimson Liquid Glass UI shell
- [x] Verify Windows release build in CI

## 0.2 — Overlay behavior
- [x] Track Minecraft window position and size
- [x] Optional follow mode while Minecraft is foreground
- [x] Optional HUD widgets only while Minecraft is focused
- [x] Optional automatic menu hide on focus loss with manual hotkey override
- [x] Detect minimized Minecraft windows and optionally hide the overlay until restored
- [ ] Reliable show / hide and focus behavior
- [x] Per-monitor DPI awareness and DPI-aware multi-monitor positioning
- [ ] Validate mixed-DPI behavior on physical Windows displays

## 0.3 — HUD and customization
- [ ] Draggable desktop HUD widgets
- [ ] Persistent JSON profiles
- [ ] Theme editor, opacity, scale, animation controls
- [ ] Custom crosshair preview and desktop widgets

## 0.4 — Windows companion features
- [ ] Instance detection for common launchers
- [ ] Per-instance profiles
- [ ] Installer and portable release

## Compatibility boundary
The desktop overlay is independent of Minecraft mod loaders. It does not inject native code into the game, patch process memory, bypass anti-cheat, or conceal itself from ordinary capture tools.


## Completed — Interface scaling
- Added a 0.8×–1.3× overlay-wide UI scale control.
- Persisted the selected scale in the local JSON profile.
- Kept compatibility with older profiles through a default scale value.


## Multi-monitor positioning
- [x] Detect the monitor containing the Minecraft window.
- [x] Read that monitor's usable work area (excluding the taskbar).
- [x] Clamp the followed overlay panel position to the same monitor's work area.


## DPI-aware multi-monitor implementation
- [x] Request Per-Monitor V2 DPI awareness before creating the native app window, with a Per-Monitor V1 fallback.
- [x] Read the Minecraft window's effective DPI.
- [x] Convert physical desktop coordinates to egui logical viewport positions.
- [x] Clamp the panel using target-monitor DPI-adjusted dimensions and its usable work area.
- [x] Preserve native DPI scale while applying the app's independent UI zoom.
- [ ] Validate on physical Windows 10/11 systems with mixed monitor scale factors.
