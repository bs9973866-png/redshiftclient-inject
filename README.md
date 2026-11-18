# REDSHIFT Inject Client

A Windows companion application and Fabric client component for Minecraft Java 1.21.11.

## Architecture

- `client/`: Fabric client-side component loaded by Minecraft's supported mod loader.
- `desktop/`: Windows companion app (planned) for profiles, configuration and process detection.
- `shared/`: shared protocol/configuration definitions (planned).

This project does **not** inject native DLLs into the JVM, patch game memory, bypass anti-cheat, or hide its presence. The supported integration path is a normal Fabric mod installed in the Minecraft instance. The companion app will manage configuration and detect a running game process without modifying it.

## Requirements

- Windows 10/11
- Java 21
- Minecraft Java 1.21.11
- Fabric Loader compatible with Minecraft 1.21.11

## Development

The first milestone establishes the Fabric client entrypoint and a lightweight in-game ClickGUI foundation. Build instructions will be added with the Gradle wrapper.

## Design

Black / Crimson / Liquid Glass: translucent layered panels, restrained highlights, smooth transitions, configurable accent color, and low-overhead rendering.
