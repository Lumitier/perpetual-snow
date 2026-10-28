package com.example.perpetualsnow.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Haengt den YACL-Screen ans Zahnrad in der Mod-Menu-Liste.
 *
 * Verifiziert gegen ModMenu-API:
 *   interface ModMenuApi { ConfigScreenFactory<?> getModConfigScreenFactory(); }
 *   interface ConfigScreenFactory<S extends Screen> { S create(Screen parent); }
 *
 * Registriert ueber den Entrypoint "modmenu" in fabric.mod.json.
 */
public final class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return PerpetualSnowScreen::create;
	}
}
