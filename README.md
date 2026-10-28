# Perpetual Snow

Clientseitige [Fabric](https://fabricmc.net/)-Mod fuer Minecraft **1.21.11**.
Laesst dauerhaft Schnee fallen — unabhaengig von Wetter, Biom und Server.
Gebaut fuer PvP: funktioniert auch in geschlossenen Arenen.

## Features
- **Dauerschnee in allen Bioms**, auch in der Wueste und bei klarem Wetter
- **Funktioniert in Arenen**: Schnee faellt auch unter Decken (PvP-Server)
- Vanilla-Regen/Gewitter wandelt den Schnee nicht zurueck in Regen
- **Respektiert Texturpack-Schnee** — es wird keine eigene Textur erzwungen
- **Skyboxify-kompatibel** (v3.4, Custom Skies) — getestet, kein Flackern
- Keine Regengeraeusche (faellt durch den Schnee-Modus automatisch weg)
- Rein clientseitig: laeuft auf fremden Servern, kein Gameplay-Einfluss,
  keine Netzwerk-Pakete

## Konfiguration
Im Spiel ueber **Mods → Perpetual Snow → Zahnrad** (benoetigt Mod Menu).
Alle Regler wirken sofort, ohne Neustart.

| Kategorie   | Optionen |
|-------------|----------|
| Allgemein   | Mod an/aus, Overworld / Nether / End einzeln |
| Aussehen    | Menge & Dichte, Flockengroesse, Deckkraft |
| Bewegung    | Fallgeschwindigkeit, seitlicher Drift |
| Umgebung    | Himmel nicht abdunkeln, Regengeraeusche stumm, **Schnee ueberall (durch Decken)**, Schnee voll beleuchten |

Der Screen folgt der Spielsprache — **Englisch und Deutsch** sind enthalten.

Die Einstellungen landen in `config/perpetualsnow.json`. Ungueltige Werte
fallen automatisch auf die Standardwerte zurueck.

> **Tipp fuer PvP:** Die Menge standardmaessig niedrig lassen (ca. 0.35) —
> dann bleibt die Sicht frei.

## Voraussetzungen
- Minecraft **1.21.11** mit **Fabric Loader 0.20.1 oder neuer**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YACL](https://modrinth.com/mod/yacl) (fuer den Config-Screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, fuer das Zahnrad)

## Installation
`perpetualsnow-1.0.0.jar` in den `mods`-Ordner kopieren, zusammen mit den
Abhaengigkeiten oben.

## Selbst bauen
Benoetigt **JDK 25** (nur zum Bauen — die Mod selbst laeuft auf Java 21).

```bash
./gradlew build        # -> build/libs/perpetualsnow-1.0.0.jar
./gradlew runClient    # Dev-Client zum Testen
```
Windows: `gradlew.bat build`.

Die Datei **ohne** `-sources` im Namen ist die spielbare Mod.

## Lizenz
Lizenziert unter der [Apache License 2.0](LICENSE).

Du darfst die Mod frei nutzen, veraendern und weitergeben, solange die
Urheberrechts- und Lizenzhinweise erhalten bleiben und Aenderungen
gekennzeichnet werden. Siehe [NOTICE](NOTICE) fuer die Lizenzen der
mitgelieferten Bibliotheken.

Minecraft ist eine Marke von Mojang Synergies AB. Dieses Projekt steht in
keiner Verbindung zu Mojang oder Microsoft.
