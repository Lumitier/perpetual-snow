package com.example.perpetualsnow.client;

import com.lumitier.lumilib.api.LumiLibApi;
import com.lumitier.lumilib.api.LumiMod;
import com.lumitier.lumilib.api.SettingsPage;
import com.example.perpetualsnow.PerpetualSnow;
import com.example.perpetualsnow.config.PerpetualSnowScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;

/**
 * Anmeldung bei LumiLib.
 *
 * WARUM EINE EIGENE KLASSE - das ist der wichtige Teil:
 * LumiLib ist OPTIONAL. Stuende der Verweis darauf im Initialisierer der
 * Mod, wuerde die Java-Laufzeit beim Start versuchen, LumiLibs Klassen
 * aufzuloesen - und ohne LumiLib startet die Mod gar nicht mehr. Liegt
 * der Verweis dagegen in einer eigenen Klasse, wird sie erst geladen,
 * wenn jemand sie anfasst. Vorher wird abgefragt, ob LumiLib da ist.
 *
 * NAME, VERSION UND BESCHREIBUNG KOMMEN AUS DER fabric.mod.json:
 * Sie stehen dort ohnehin, Mod Menu und Modrinth lesen dieselben Werte.
 * Sie hier noch einmal einzutragen hiesse, zwei Stellen gleich halten zu
 * muessen - und nach dem naechsten Versionssprung stuende im Hub die
 * alte Nummer.
 *
 * ZUM ICON: nichts zu tun. LumiLib liest es aus dem Jar dieser Mod.
 */
public final class LumiLibCompat {

	private LumiLibCompat() {}

	public static void register() {
		ModContainer container = FabricLoader.getInstance()
			.getModContainer(PerpetualSnow.MOD_ID).orElse(null);

		String version = container == null ? ""
			: container.getMetadata().getVersion().getFriendlyString();
		String name = container == null ? "Perpetual Snow"
			: container.getMetadata().getName();
		String summary = container == null ? ""
			: container.getMetadata().getDescription();

		LumiLibApi.register(new LumiMod(
			PerpetualSnow.MOD_ID,
			Text.literal(name),
			version,
			Text.literal(summary),
			// Kein eigenes Sprite - LumiLib nimmt das Icon aus dem Jar.
			null,
			// Der bestehende YACL-Bildschirm. Sobald LumiLib Schieberegler
			// und Farbwaehler kann, wird daraus eine SettingsPage und die
			// Einstellungen stehen direkt im Hub.
			// YACL bleibt als Rueckfallebene - Mod Menus Zahnrad
			// oeffnet weiterhin diesen Bildschirm.
			PerpetualSnowScreen::create,
			LumiLibCompat::settings));

		// Server-Regeln (LumiLibControlPlugin). Eigener Fang: Mit einer
		// LumiLib aelter als 1.3.0 gibt es die Methode nicht - dann soll die
		// Mod trotzdem im Hub stehen, nur eben ohne Server-Steuerung.
		try {
			LumiLibApi.onServerRulesChanged(() ->
				PerpetualSnow.serverDisabled = LumiLibApi.isDisabledByServer(PerpetualSnow.MOD_ID));
		} catch (Throwable t) {
			PerpetualSnow.LOGGER.warn("[Perpetual Snow] LumiLib is too old for server control (needs 1.3.0+).");
		}
	}

	/**
	 * Die Einstellungen, wie LumiLibs Hub sie zeichnet.
	 *
	 * DIESELBEN FELDER wie der YACL-Bildschirm, nur anders beschrieben.
	 * Beide Wege schreiben direkt in die statischen Felder von
	 * PerpetualSnow und rufen dasselbe save() - es gibt also nur EINEN
	 * Stand, egal ueber welchen Weg man ihn aendert.
	 *
	 * Die Texte kommen aus derselben Sprachdatei wie bisher,
	 * perpetualsnow.option.* und perpetualsnow.group.*.
	 */
	private static SettingsPage settings() {
		return SettingsPage.builder(PerpetualSnow.MOD_ID)
			.section("general")
				.toggle("enabled",
					() -> PerpetualSnow.enabled, v -> PerpetualSnow.enabled = v, true)
			.section("dimensions")
				.toggle("overworld",
					() -> PerpetualSnow.overworld, v -> PerpetualSnow.overworld = v, true)
				.toggle("nether",
					() -> PerpetualSnow.nether, v -> PerpetualSnow.nether = v, false)
				.toggle("end",
					() -> PerpetualSnow.end, v -> PerpetualSnow.end = v, false)
			.section("appearance")
				.slider("amount", 0.0F, 1.0F, 0.05F,
					() -> PerpetualSnow.amount, v -> PerpetualSnow.amount = v, 0.35F)
				.slider("flakeSize", 0.2F, 3.0F, 0.1F,
					() -> PerpetualSnow.flakeSize, v -> PerpetualSnow.flakeSize = v, 1.0F)
				.slider("opacity", 0.1F, 1.0F, 0.05F,
					() -> PerpetualSnow.opacity, v -> PerpetualSnow.opacity = v, 1.0F)
			.section("motion")
				.slider("fallSpeed", 0.2F, 3.0F, 0.1F,
					() -> PerpetualSnow.fallSpeed, v -> PerpetualSnow.fallSpeed = v, 1.0F)
				.slider("drift", 0.0F, 3.0F, 0.1F,
					() -> PerpetualSnow.drift, v -> PerpetualSnow.drift = v, 1.0F)
			.section("environment")
				.toggle("noSkyDarken",
					() -> PerpetualSnow.noSkyDarken, v -> PerpetualSnow.noSkyDarken = v, true)
				.toggle("muteRainSounds",
					() -> PerpetualSnow.muteRainSounds, v -> PerpetualSnow.muteRainSounds = v, true)
				.toggle("throughCeilings",
					() -> PerpetualSnow.snowThroughBarriers,
					v -> PerpetualSnow.snowThroughBarriers = v, true)
				.toggle("fullBright",
					() -> PerpetualSnow.fullBright, v -> PerpetualSnow.fullBright = v, true)
			.save(PerpetualSnow::save)
			.build();
	}
}
