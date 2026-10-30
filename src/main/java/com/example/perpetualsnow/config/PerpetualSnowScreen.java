package com.example.perpetualsnow.config;

import com.example.perpetualsnow.PerpetualSnow;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Baut den YACL-Options-Screen fuer Perpetual Snow.
 *
 * Alle Texte laufen ueber Uebersetzungsschluessel (assets/perpetualsnow/lang/).
 * Vorhanden: en_us, de_de. Der Screen folgt damit automatisch der Spielsprache.
 *
 * Verifiziert gegen YACL 3.8.x API (dev.isxander.yacl3.api):
 *   YetAnotherConfigLib.createBuilder().title(..).category(..).save(..).build().generateScreen(parent)
 *   Option.<T>createBuilder().name(..).binding(default, getter, setter).controller(..).build()
 *   FloatSliderControllerBuilder.create(opt).range(min,max).step(s)
 *   BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true)
 *
 * Alle Bindings schreiben DIREKT in die statischen Felder von PerpetualSnow
 * (Live-Wirkung). save() schreibt die JSON-Datei.
 */
public final class PerpetualSnowScreen {

	private PerpetualSnowScreen() {}

	/** Uebersetzungsschluessel-Praefix fuer eine Option. */
	private static Text key(String id) {
		return Text.translatable("perpetualsnow.option." + id);
	}

	private static OptionDescription desc(String id) {
		return OptionDescription.createBuilder()
			.text(Text.translatable("perpetualsnow.option." + id + ".desc"))
			.build();
	}

	private static Option<Boolean> toggle(String id, boolean def,
	                                      java.util.function.Supplier<Boolean> getter,
	                                      java.util.function.Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
			.name(key(id))
			.description(desc(id))
			.binding(def, getter, setter)
			.controller(opt -> BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true))
			.build();
	}

	private static Option<Float> slider(String id,
	                                    float def, float min, float max, float step,
	                                    java.util.function.Supplier<Float> getter,
	                                    java.util.function.Consumer<Float> setter) {
		return Option.<Float>createBuilder()
			.name(key(id))
			.description(desc(id))
			.binding(def, getter, setter)
			.controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step))
			.build();
	}

	public static Screen create(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
			.title(Text.translatable("perpetualsnow.title"))

			// ================= Tab 1: General =================
			.category(ConfigCategory.createBuilder()
				.name(Text.translatable("perpetualsnow.category.general"))
				.option(toggle("enabled", true,
					() -> PerpetualSnow.enabled, v -> PerpetualSnow.enabled = v))
				.build())

			// ================= Tab 2: Perpetual Snow =================
			.category(ConfigCategory.createBuilder()
				.name(Text.translatable("perpetualsnow.category.snow"))

				.group(OptionGroup.createBuilder()
					.name(Text.translatable("perpetualsnow.group.dimensions"))
					.option(toggle("overworld", true,
						() -> PerpetualSnow.overworld, v -> PerpetualSnow.overworld = v))
					.option(toggle("nether", false,
						() -> PerpetualSnow.nether, v -> PerpetualSnow.nether = v))
					.option(toggle("end", false,
						() -> PerpetualSnow.end, v -> PerpetualSnow.end = v))
					.build())

				.group(OptionGroup.createBuilder()
					.name(Text.translatable("perpetualsnow.group.appearance"))
					.option(slider("amount", 0.35F, 0.0F, 1.0F, 0.05F,
						() -> PerpetualSnow.amount, v -> PerpetualSnow.amount = v))
					.option(slider("flakeSize", 1.0F, 0.2F, 3.0F, 0.1F,
						() -> PerpetualSnow.flakeSize, v -> PerpetualSnow.flakeSize = v))
					.option(slider("opacity", 1.0F, 0.1F, 1.0F, 0.05F,
						() -> PerpetualSnow.opacity, v -> PerpetualSnow.opacity = v))
					.build())

				.group(OptionGroup.createBuilder()
					.name(Text.translatable("perpetualsnow.group.motion"))
					.option(slider("fallSpeed", 1.0F, 0.2F, 3.0F, 0.1F,
						() -> PerpetualSnow.fallSpeed, v -> PerpetualSnow.fallSpeed = v))
					.option(slider("drift", 1.0F, 0.0F, 3.0F, 0.1F,
						() -> PerpetualSnow.drift, v -> PerpetualSnow.drift = v))
					.build())

				.group(OptionGroup.createBuilder()
					.name(Text.translatable("perpetualsnow.group.environment"))
					.option(toggle("noSkyDarken", true,
						() -> PerpetualSnow.noSkyDarken, v -> PerpetualSnow.noSkyDarken = v))
					.option(toggle("muteRainSounds", true,
						() -> PerpetualSnow.muteRainSounds, v -> PerpetualSnow.muteRainSounds = v))
					.option(toggle("throughCeilings", true,
						() -> PerpetualSnow.snowThroughBarriers, v -> PerpetualSnow.snowThroughBarriers = v))
					.option(toggle("fullBright", true,
						() -> PerpetualSnow.fullBright, v -> PerpetualSnow.fullBright = v))
					.build())

				.build())

			.save(PerpetualSnow::save)
			.build()
			.generateScreen(parent);
	}
}
