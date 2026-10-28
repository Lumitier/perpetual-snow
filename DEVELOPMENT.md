# Entwickler-Notizen — Perpetual Snow

Technische Notizen zum Projekt: verifizierte Versionen, Architektur,
und welche Ansaetze NICHT funktioniert haben (damit sie niemand wiederholt).

## Was das ist
Clientseitige Fabric-Mod: dauerhafter Schneefall, unabhaengig von Wetter, Biom
und Server. Nur Client, keine Netzwerk-Pakete, kein Gameplay-Einfluss. Muss auf
fremden Servern (PvP) laufen, auch nach Teleport / Dimensionswechsel / Respawn /
Reconnect.

## Verifizierte Versionen (Stand 2026-10-27)
| Komponente        | Wert               | Quelle / Status |
|-------------------|--------------------|-----------------|
| Minecraft         | `1.21.11`          | verifiziert (FabricMC/fabric-example-mod, Branch 1.21.11) |
| Yarn Mappings     | `1.21.11+build.6`  | verifiziert (FabricMC/yarn Tags, hoechster Release-Build) |
| Fabric Loader     | `0.20.1`           | verifiziert (offizielle Beispielmod) |
| Fabric API        | `0.141.6+1.21.11`  | verifiziert (offizielle Beispielmod) |
| Loom (Plugin)     | `1.19.3`           | verifiziert (Build loeste Snapshot zu 1.19.3 auf). Verlangt Gradle >= 9.8.0. |
| Gradle (Wrapper)  | `9.8.2`            | Pflicht wegen Loom 1.19.3 (9.8 existiert nicht als Download) |
| Java (Build)      | `25`               | Loom 1.19.x ist auf Java 25 kompiliert; Mod-Code bleibt auf Level 21 |
| YACL              | `3.8.2+1.21.11-fabric` | Config-UI (Modrinth-Maven: `maven.modrinth:yacl:pHWDw3Vc`) |
| Mod Menu          | `17.0.1`           | Zahnrad in der Mod-Liste (`maven.modrinth:modmenu:cLkYaVuL`) |
| quilt-parsers     | `0.2.1`            | YACL-Laufzeitabhaengigkeit, muss mit-gebuendelt werden |
| Java              | `21`               | Pflicht |

**Mappings-Hinweis:** Dieses Projekt nutzt **Yarn** (wie urspruenglich gewuenscht).
Die offizielle Fabric-Beispielmod ist inzwischen auf Mojang-Mappings
(`loom.officialMojangMappings()`) + `fabric-loom-remap` umgestellt. Falls wir
spaeter auf Mojmap wechseln wollen, aendern sich Klassen-/Methodennamen im Code
(Yarn-Namen != Mojmap-Namen). Entscheidung bewusst bei Yarn gelassen.

## Build / Run
Gebaut und getestet wird lokal (JDK 25 noetig, siehe unten).

```bash
# Einmalig: Wrapper-Jar erzeugen (fehlt im Zip, weil services.gradle.org
# im Workspace blockiert war). Lokaler Gradle noetig, einmalig:
gradle wrapper --gradle-version 8.14.3

# Danach immer ueber den Wrapper:
./gradlew build          # baut den Mod-Jar nach build/libs/
./gradlew runClient      # startet einen Dev-Client mit der Mod
./gradlew genSources     # dekompiliert MC-Quellen (fuer Mixin-Target-Recherche)
```

Windows: `gradlew.bat build` bzw. `gradlew.bat runClient`.

Fertiger Mod-Jar landet in `build/libs/perpetualsnow-<version>.jar` (NICHT der
`-sources.jar`).

## Architektur / Konventionen
- Package-Root: `com.example.perpetualsnow`
  - `client/`  — Entrypoint + Client-Logik
  - `mixin/`   — Mixins (Config: `perpetualsnow.mixins.json`, alle `client`)
  - `config/`  — Config-Modell + Screen (ab Phase 4)
- Mixin-Regeln: so klein wie moeglich. `@Inject`/`@Redirect`/`@ModifyVariable`
  bevorzugt, `@Overwrite` nur im Notfall.
- Performance: keine Allokationen pro Frame in Hot Paths. Obergrenze fuer Schneemenge.
- Config-Werte clampen; ungueltige Config faellt robust auf Defaults zurueck.
- Keine erfundenen APIs. Jedes Mixin-Target muss gegen echten 1.21.11-Code
  verifiziert sein. Unsicherheiten im Code als Kommentar markieren, nicht raten.

## Phasenplan
1. **Setup** (erledigt): Build-Geruest, laedt im Client, keine Funktion.
2. Analyse (erledigt): Ansatz A gewaehlt (Vanilla-Wetter hooken). Kernmethode
   WeatherRendering.buildPrecipitationPieces: state.intensity = getRainGradient();
   Gate bei intensity<=0; getPrecipitationAt() entscheidet RAIN/SNOW/NONE.
3. Minimalversion hartcodiert (erledigt): Zwei MixinExtras-Hooks auf WeatherRendering
   - @ModifyExpressionValue auf getRainGradient -> Intensitaet/Menge hochziehen (Gate oeffnen)
   - @ModifyReturnValue auf getPrecipitationAt -> immer SNOW
   Werte hartcodiert in PerpetualSnow.java (enabled, overworld/nether/end, intensity=0.35).
   MixinExtras explizit eingebunden + via include() gebuendelt (Loader liefert es nur
   im development-Scope). mavenCentral() fuer die Aufloesung ergaenzt.
4. Config-Screen (erledigt): YACL 3.8.2+1.21.11-fabric + Mod Menu 17.0.1.
   - PerpetualSnow.java: alle Config-Felder + GSON-Persistenz (config/perpetualsnow.json),
     clamp/sanitize, load()/save().
   - config/PerpetualSnowScreen.java: YACL-Screen (Kategorien Allgemein/Aussehen/
     Bewegung/Umgebung), Bindings schreiben direkt in die statischen Felder (live).
   - config/ModMenuIntegration.java: Zahnrad -> Screen (Entrypoint "modmenu").
   - Mixins erweitert: Menge (getRainGradient-Hook), Flockengroesse (CONSTANT 2.0F
     Divisor-Hook, UNSICHER), Deckkraft (ColorHelper.getWhite-Hook), Fall+Drift
     (createSnowPiece-ReturnValue, neues Piece-Record), SNOW-Force (bestehend).
   - Bezug von YACL/ModMenu ueber Modrinth-Maven (maven.modrinth:yacl:pHWDw3Vc,
     maven.modrinth:modmenu:cLkYaVuL) + isXander/TerraformersMC Maven als Repos.
   Abweichung vom urspruenglichen Plan: statt eigenem Lib-freiem Screen direkt
   YACL+ModMenu (Nutzerwunsch: clean bedienbare Slider, verifizierter 1.21.11-Support).
   OneConfig wurde geprueft (1.5.0 fuer 1.21.11-fabric existiert), aber verworfen:
   braucht das invasive dev.deftu.gradle-Toolkit (DGT), zu viel Risiko fuer diese Mod.
5. Kompatibilitaet + Haertung (erledigt): Arena-Decken-Problem geloest.
   PROBLEM: In geschlossenen Arenen liefert getTopY die DECKE statt des Bodens ->
   o == p -> Gate (p-o != 0) schlaegt zu -> gar kein Schnee.
   VERWORFENE ANSAETZE (dokumentiert, damit sie niemand wiederholt):
   - "nur Barrier-Bloecke ignorieren": Barrier zaehlt NICHT fuer die MOTION_BLOCKING-
     Heightmap (per Debug-Log verifiziert: getTopY lieferte Boden, nicht Barriere).
     Manuelles Spalten-Scannen war zudem fragil (Scan-Reichweite, Blocktyp, Server).
     Die realen Arena-Decken des Nutzers sind offenbar normale feste Bloecke, keine
     Barrier -> es gab schlicht nichts zu erkennen.
   - @Local(ordinal=..) fuer die Spalten-Koordinaten: lieferte Werte aus falschen
     Iterationen (Debug zeigte x=-63 statt Spielerposition). NICHT verwenden.
   LOESUNG: @Redirect auf World.getTopY in buildPrecipitationPieces gibt
   world.getBottomY() zurueck, wenn die Option aktiv ist. Dadurch wird
   o = camY-radius, p = camY+radius -> Schnee faellt IMMER um den Spieler herum,
   unabhaengig von jeder Decke. Keine Block-Lookups, keine Hoehengrenze.
   Config: snowThroughBarriers ("Schnee ueberall (durch Decken)"), Default an.
   Zusaetzlich: fullBright (Schnee voll beleuchtet, sonst unter Decken dunkel).
   GETESTET: Arena auf Y40 mit Skyboxify v3.4 + Sky-Texturepack -> Schnee und
   Custom Sky laufen gleichzeitig, kein Flackern, keine Reihenfolge-Probleme.
   Nutzer-Modpack: 220 Mods, 1.21.11; Fabric Loader im Pack musste von 0.19.5
   auf >=0.20.1 angehoben werden (Loader-Update im Launcher).
6. Release (erledigt): Version 1.0.0, Debug-Code entfernt, README/DEVELOPMENT.md aktuell.
   i18n: Config-Screen nutzt Uebersetzungsschluessel (assets/perpetualsnow/lang/).
   Vorhanden en_us + de_de, beide 32 Schluessel, vollstaendig abgeglichen.
   Neue Option IMMER in BEIDEN lang-Dateien ergaenzen (.name und .desc).

## Kompatibilitaetsziele
- Skyboxify v3.4 (Custom Skies): ERFUELLT - gemeinsam getestet, kein Konflikt.
- Rendering-Mods: laeuft im 220-Mod-Pack des Nutzers auf 1.21.11.
- Texturpack-Schnee: erfuellt (Vanilla rendert weiter mit SNOW_TEXTURE).
