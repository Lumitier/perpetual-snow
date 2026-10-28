package com.example.perpetualsnow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mod-weiter Zustand + Config.
 *
 * Bewusst statische Felder: die Mixins im Hot-Path (pro Frame) lesen sie ohne
 * Allokation und ohne Objekt-Lookup. Die YACL-Oberflaeche schreibt direkt in
 * diese Felder (Live-Wirkung), und save()/load() persistiert sie als JSON unter
 * config/perpetualsnow.json.
 *
 * Alle Werte werden beim Setzen und beim Laden geclampt; ungueltige/kaputte
 * Config faellt auf die Defaults zurueck.
 */
public final class PerpetualSnow {

	public static final String MOD_ID = "perpetualsnow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private PerpetualSnow() {}

	// ------------------------------------------------------------------
	// Config-Felder (Defaults = PvP-taugliche, dezente Werte)
	// ------------------------------------------------------------------

	/** Mod global an/aus. */
	public static volatile boolean enabled = true;

	/** Schnee in der Overworld. */
	public static volatile boolean overworld = true;
	/** Schnee im Nether. */
	public static volatile boolean nether = false;
	/** Schnee im End. */
	public static volatile boolean end = false;

	/**
	 * Menge/Dichte (0..1). Treibt das Render-Gate (Vanilla rendert nur bei >0)
	 * und zugleich die Intensitaet. Niedrig = klare Sicht beim PvP.
	 */
	public static volatile float amount = 0.35F;

	/** Flockengroesse/-dicke Faktor (0.2..3.0), 1.0 = Vanilla. */
	public static volatile float flakeSize = 1.0F;

	/** Fallgeschwindigkeit Faktor (0.2..3.0), 1.0 = Vanilla. */
	public static volatile float fallSpeed = 1.0F;

	/** Seitlicher Drift Faktor (0..3.0), 1.0 = Vanilla. */
	public static volatile float drift = 1.0F;

	/** Deckkraft/Alpha (0.1..1.0), 1.0 = voll. */
	public static volatile float opacity = 1.0F;

	/** Himmel/Nebel bei Pseudo-Wetter NICHT abdunkeln. */
	public static volatile boolean noSkyDarken = true;

	/** Regengeraeusche stummschalten. */
	public static volatile boolean muteRainSounds = true;

	/**
	 * Durch Barrier-Bloecke schneien (PvP). Viele PvP-Server spannen eine
	 * Barrier-Decke (oder Light-Bloecke) ueber die Arena. Vanilla erzeugt Schnee
	 * nur bis zum obersten blockenden Block (getTopY) -> ist das eine Barriere,
	 * faellt darunter nichts.
	 *
	 * Ist diese Option an, behandeln wir NUR Barrier-/Light-Bloecke als
	 * "nicht vorhanden": steht so ein Block oben, suchen wir den naechsten echten
	 * Block darunter, sodass der Schnee durch die Barriere faellt. Echte Daecher
	 * (Stein, Holz, Glas ...) halten den Schnee weiterhin ab.
	 */
	public static volatile boolean snowThroughBarriers = true;

	/**
	 * Schnee immer voll beleuchten. Unter einer Barrier-Decke ist das Himmelslicht
	 * reduziert -> Vanilla rendert den Schnee dort dunkel/unsichtbar. Diese Option
	 * zwingt die Flocken auf volle Helligkeit, damit sie unter Decken (PvP-Arenen)
	 * klar sichtbar sind.
	 */
	public static volatile boolean fullBright = true;

	// ------------------------------------------------------------------
	// Clamp-Helfer (nie NaN, immer im Bereich)
	// ------------------------------------------------------------------

	public static float clamp(float v, float min, float max) {
		if (Float.isNaN(v)) {
			return min;
		}
		if (v < min) {
			return min;
		}
		if (v > max) {
			return max;
		}
		return v;
	}

	/** Alle Felder nach dem Laden/Setzen in gueltige Bereiche zwingen. */
	public static void sanitize() {
		amount    = clamp(amount, 0.0F, 1.0F);
		flakeSize = clamp(flakeSize, 0.2F, 3.0F);
		fallSpeed = clamp(fallSpeed, 0.2F, 3.0F);
		drift     = clamp(drift, 0.0F, 3.0F);
		opacity   = clamp(opacity, 0.1F, 1.0F);
	}

	// ------------------------------------------------------------------
	// Persistenz (GSON kommt mit Minecraft)
	// ------------------------------------------------------------------

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
	}

	/** Reines Daten-Transferobjekt fuer JSON. */
	private static final class Data {
		boolean enabled, overworld, nether, end, noSkyDarken, muteRainSounds, snowThroughBarriers, fullBright;
		float amount, flakeSize, fallSpeed, drift, opacity;
	}

	public static void load() {
		Path p = configPath();
		try {
			if (Files.exists(p)) {
				try (Reader r = Files.newBufferedReader(p)) {
					Data d = GSON.fromJson(r, Data.class);
					if (d != null) {
						enabled = d.enabled;
						overworld = d.overworld;
						nether = d.nether;
						end = d.end;
						noSkyDarken = d.noSkyDarken;
						muteRainSounds = d.muteRainSounds;
						snowThroughBarriers = d.snowThroughBarriers;
						fullBright = d.fullBright;
						amount = d.amount;
						flakeSize = d.flakeSize;
						fallSpeed = d.fallSpeed;
						drift = d.drift;
						opacity = d.opacity;
					}
				}
			}
		} catch (Exception e) {
			// Kaputte Config -> Defaults behalten, Fehler nur loggen.
			LOGGER.warn("[Perpetual Snow] Config konnte nicht geladen werden, nutze Defaults.", e);
		}
		sanitize();
		// Direkt zurueckschreiben, damit eine frische/reparierte Datei existiert.
		save();
	}

	public static void save() {
		sanitize();
		Path p = configPath();
		try {
			Files.createDirectories(p.getParent());
			Data d = new Data();
			d.enabled = enabled;
			d.overworld = overworld;
			d.nether = nether;
			d.end = end;
			d.noSkyDarken = noSkyDarken;
			d.muteRainSounds = muteRainSounds;
			d.snowThroughBarriers = snowThroughBarriers;
			d.fullBright = fullBright;
			d.amount = amount;
			d.flakeSize = flakeSize;
			d.fallSpeed = fallSpeed;
			d.drift = drift;
			d.opacity = opacity;
			try (Writer w = Files.newBufferedWriter(p)) {
				GSON.toJson(d, w);
			}
		} catch (Exception e) {
			LOGGER.warn("[Perpetual Snow] Config konnte nicht gespeichert werden.", e);
		}
	}
}
