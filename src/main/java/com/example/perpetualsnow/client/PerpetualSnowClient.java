package com.example.perpetualsnow.client;

import net.fabricmc.api.ClientModInitializer;
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
		com.example.perpetualsnow.PerpetualSnow.load();
		LOGGER.info("[Perpetual Snow] geladen - Dauerschnee aktiv, Config bereit.");
	}
}
