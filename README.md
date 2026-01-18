# Ping

[![GitHub last commit](https://img.shields.io/github/last-commit/zbejas/Ping)](https://github.com/zbejas/Ping/commits/master)
[![GitHub issues](https://img.shields.io/github/issues/zbejas/Ping)](https://github.com/zbejas/Ping/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/zbejas/Ping)](https://github.com/zbejas/Ping/pulls)
[![GitHub license](https://img.shields.io/github/license/zbejas/Ping)](https://github.com/zbejas/Ping/blob/master/LICENSE.md)
[![Release](https://img.shields.io/github/v/release/zbejas/Ping)](https://github.com/zbejas/Ping/releases)

**Ping** is a Hytale server plugin that allows players to mark and highlight locations in the world, making it easier to communicate with teammates by showing them exactly where you're looking.

The plugin was made to simplify coordination and communication in multiplayer Hytale servers. It provides visual and audio feedback when marking locations, helping teams work together more effectively. Also, when I switched from Arc Raiders, I missed the middle mouse button ping functionality, so I decided to create this plugin to bring that feature here.

## Features

- **Visual Ping System**: Create visual markers at any location you're looking at
- **Particle Effects**: Customizable particle effects that appear at pinged locations
- **Sound Feedback**: Audio cues when pings are created
- **Raycast Detection**: Intelligent raycasting to detect target locations (up to 500 blocks)
- **Temporary Markers**: Pings automatically disappear after a configurable duration
- **Configurable Settings**: Full control over ping appearance, sounds, and behavior
- **Debug Mode**: Built-in debugging tools for development and troubleshooting

## Installation

### Before you start

You will need to set a few things up before you can run the plugin:

- A Hytale server with plugin support
- Java 25 or higher installed on your server (Hytale recommends Java 25+)
- Access to your server's `mods/` directory

### Installing the Plugin

1. Download the latest `.jar` file from the [releases page](https://github.com/zbejas/Ping/releases)
2. Place the jar file in your Hytale server's `mods/` directory
3. Restart your server
4. The plugin will automatically generate a `PingConfig.json` file on first run

> [!TIP]
> After installation, you can customize the plugin behavior by editing the `PingConfig.json` file. See the [Configuration](#configuration) section for details.

## Usage

To create a ping, simply look at the desired location and perform the "Pick" interaction (default keybind: `Middle Mouse Button`).

The plugin will:

1. Detect the Pick interaction
2. Perform a raycast to find the targeted location (up to x blocks away)
3. Spawn particle effects at that location
4. Play sound effects to provide feedback
5. Maintain the ping for the configured duration

> [!NOTE]
> The ping system has a minimum distance requirement. Close-range Pick interactions won't create pings, allowing normal block/item picking to work as expected.

## Configuration

The plugin creates a `PingConfig.json` file with the following options:

### General Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `DebugMode` | boolean | `false` | Enable debug logging for troubleshooting |

### Ping Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `PlayerEyeHeightOffset` | double | `1.6` | Eye height offset for raycast origin |
| `HeightOffset` | double | `1.0` | Vertical offset for ping position |
| `HorizontalOffset` | double | `0.5` | Horizontal offset for ping position |
| `DistanceOffset` | double | `1.0` | Distance offset for ping position |
| `ParticleEffect` | string | `"Shield_Block"` | Particle effect for ping markers |
| `SoundEvent` | string | `"SFX_Crystal_Break"` | Sound played when creating a ping |
| `DurationSeconds` | integer | `10` | How long pings remain visible (seconds) |
| `IntervalMs` | long | `500` | Time between particle effect updates (milliseconds) |
| `DebugParticleEffect` | string | `"BeamEmiter_Heal_Red"` | Particle effect used in debug mode |
| `MaxDistance` | integer | `500` | Maximum distance for detecting blocks |

## Building from Source

> [!IMPORTANT]
> The following instructions are for building the plugin manually. If you just want to use the plugin, download a pre-built jar from the [releases page](https://github.com/zbejas/Ping/releases).

### Prerequisites

- JDK 25 or higher
- Gradle (wrapper included)

### Build Instructions

1. Clone the repository:

    ```bash
    git clone https://github.com/zbejas/Ping.git
    ```

2. Download Hytale Server jar and place it in the `libs/` directory:
   - For this step, you can manually copy the `HytaleServer.jar` from your Hytale installation, or use the Hytale Downloader CLI.
3. Build the project using Gradle:

    ```bash
    ./gradlew build
    ```

The compiled jar will be located in `build/libs/`.

> [!TIP]
> You can modify the deployment settings in the `build.gradle.kts` file to match your server configuration.

## How It Works

1. **Packet Interception**: The plugin registers a packet listener to intercept player interaction packets
2. **Interaction Detection**: When a "Pick" interaction is detected, the plugin checks if it's targeting empty space
3. **Raycast**: Performs a raycast from the player's eye position to find the target location
4. **Effect Spawning**: Spawns particle effects and plays sounds at the detected location
5. **Scheduled Updates**: Uses a scheduled executor to maintain the ping effect for the configured duration

## Troubleshooting

If you encounter any issues, here are some common problems and their solutions:

- **Pings not appearing**: Make sure the particle effect specified in the configuration exists in your Hytale installation. Try using the default `Shield_Block` particle effect.

- **Sound not playing**: Verify that the sound event specified in the configuration is valid. The default `SFX_Crystal_Break` should work in most cases.

- **Raycast not detecting targets**: Ensure you're looking at a location within the 500-block maximum distance. The plugin requires a clear line of sight to the target location.

- **Configuration not saving**: Check that the server has write permissions to the plugin's configuration directory.

>[!TIP]
> Try turning on `DebugMode` in the configuration file to get more detailed logs.

If you encounter issues not listed here, please open a new [issue](https://github.com/zbejas/Ping/issues) with details about your problem.
