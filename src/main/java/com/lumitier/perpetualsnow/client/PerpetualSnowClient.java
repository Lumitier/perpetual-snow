package com.lumitier.perpetualsnow.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-Entrypoint der Mod.
 *
 * Phase 1: Nur Lade-Nachweis. Noch keine Funktion.
 * Die eigentliche Schnee-Logik kommt ab Phase 3.
 */
public class PerpetualSnowClient implements ClientModInitializer {

	public static final String MOD_ID = "perpetualsnow";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		com.lumitier.perpetualsnow.PerpetualSnow.load();
		LOGGER.info("[Perpetual Snow] geladen - Dauerschnee aktiv, Config bereit.");

		// LumiLib ist optional - deshalb erst fragen, dann anfassen.
		// Der Fang um Throwable ist Absicht: LumiLib hat einen eigenen
		// Versionsrhythmus. Aendert sich dort etwas, soll DIESE Mod
		// weiterlaufen und nur nicht im Hub auftauchen.
		if (FabricLoader.getInstance().isModLoaded("lumilib")) {
			try {
				LumiLibCompat.register();
			} catch (Throwable t) {
				LOGGER.warn("[Perpetual Snow] LumiLib-Anmeldung fehlgeschlagen.", t);
			}
		}
	}
}
