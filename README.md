# Perpetual Snow

Client-side [Fabric](https://fabricmc.net/) mod for Minecraft **1.21.11**.
Makes snow fall permanently — regardless of weather, biome or server.
Built with PvP in mind: it also works inside closed arenas.

## Features
- **Permanent snow in every biome**, deserts included, under a clear sky
- **Works in arenas**: snow falls under ceilings too (PvP servers)
- Vanilla rain and thunderstorms won't turn the snow back into rain
- **Respects resource pack snow** — no custom texture is forced
- **Skyboxify compatible** (v3.4, Custom Skies) — tested, no flicker
- No rain sounds (the snow mode removes them automatically)
- Purely client-side: works on any server, no gameplay changes, no network
  packets

## Configuration
In game via **Mods → Perpetual Snow → ⚙** (requires Mod Menu).
Every setting applies instantly, no restart needed.

| Category    | Options |
|-------------|---------|
| General     | Mod on/off, Overworld / Nether / The End individually |
| Appearance  | Amount & density, flake size, opacity |
| Motion      | Fall speed, sideways drift |
| Environment | Don't darken the sky, mute rain sounds, **snow everywhere (through ceilings)**, fully lit snow |

Available in **English** and **German** — the screen follows your game
language.

Settings live in `config/perpetualsnow.json`. Invalid values fall back to
the defaults automatically.

> **PvP tip:** keep the amount low (around 0.35) so your view stays clear.

## Why "snow everywhere"?
Vanilla only renders weather down to the highest solid block in each column.
Inside a roofed arena that block **is** the roof, so the game decides there
is nothing to draw and you get no snow at all. The "snow everywhere" option
makes the renderer ignore ceilings.

## Requirements
- Minecraft **1.21.11** with **Fabric Loader 0.20.1** or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl) (for the config screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the settings
  button)

## Installation
Drop `perpetualsnow-1.0.0.jar` into your `mods` folder, together with the
dependencies above.

## Building it yourself
Requires **JDK 25** (to build only — the mod itself runs on Java 21).

```bash
./gradlew build        # -> build/libs/perpetualsnow-1.0.0.jar
./gradlew runClient    # dev client for testing
```
On Windows: `gradlew.bat build`.

The file **without** `-sources` in its name is the playable mod.

## License
Licensed under the [Apache License 2.0](LICENSE).

You may use, modify and redistribute this mod freely, as long as the
copyright and license notices are kept and changes are marked. See
[NOTICE](NOTICE) for the licenses of the bundled libraries.

Minecraft is a trademark of Mojang Synergies AB. This project is not
affiliated with Mojang or Microsoft.
