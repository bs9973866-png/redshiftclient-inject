# REDSHIFT roadmap

## 0.1 — Standalone overlay foundation
- [x] Replace Fabric project structure with Rust desktop application
- [x] Transparent always-on-top window
- [x] Right Shift global hotkey
- [x] Basic Minecraft window-title detection
- [x] Black / Crimson Liquid Glass UI shell
- [ ] Verify Windows release build in CI

## 0.2 — Overlay behavior
- [ ] Track Minecraft window position and size
- [ ] Show overlay only while a Minecraft window is active (user setting)
- [ ] Reliable show / hide and focus behavior
- [ ] Multi-monitor and DPI scaling support

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
