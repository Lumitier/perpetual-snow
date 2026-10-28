package com.example.perpetualsnow.mixin;

import com.example.perpetualsnow.PerpetualSnow;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Kern von Ansatz A: minimale, gezielte Eingriffe in Vanillas Wetter-Renderer.
 * Alles gegen dekompilierten MC 1.21.11 (Yarn 1.21.11+build.6) verifiziert.
 *
 * Relevante Vanilla-Methoden (WeatherRendering):
 *   buildPrecipitationPieces: state.intensity = world.getRainGradient(tp); GATE bei <=0
 *                             n = getTopY(...); o = max(camY-r,n); p = max(camY+r,n)
 *   getPrecipitationAt(world,pos): RAIN/SNOW/NONE
 *   createSnowPiece(...) -> new Piece(x,z,yMin,yMax, uOffset=g(drift), vOffset=i+h(fall), light)
 *   renderPieces(..., intensity, range, gradient):
 *       j = lerp(...) * gradient;  k = ColorHelper.getWhite(j)   // j = Alpha/Deckkraft
 *       m = NORMAL_LINE_DX[l] / 2;  n = NORMAL_LINE_DZ[l] / 2     // halbe Flockenbreite
 *
 * Hinweis Regengeraeusche: Da getPrecipitationAt durch uns IMMER SNOW liefert,
 * laeuft Vanillas Regen-Partikel-/Sound-Zweig (der auf == RAIN prueft) nie ->
 * Regengeraeusche sind damit ohnehin aus. muteRainSounds ist dadurch faktisch
 * schon erfuellt; ein eigener Sound-Hook waere toter Code.
 */
@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {

	// --- 1) Menge/Dichte + Gate ---------------------------------------
	@ModifyExpressionValue(
		method = "buildPrecipitationPieces",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRainGradient(F)F")
	)
	private float perpetualsnow$forceAmount(float original, World world) {
		if (!active(world)) {
			return original;
		}
		return Math.max(original, PerpetualSnow.clamp(PerpetualSnow.amount, 0.0F, 1.0F));
	}

	// --- 1b) Schnee ueberall, durch alle Decken (PvP-Arenen) ----------
	// ROBUSTER ANSATZ: Statt die Decke zu suchen und praezise drunter zu kappen
	// (fragil - haengt an Blocktyp, Hoehe, Server-Verhalten), machen wir Vanilla
	// das Decken-Problem komplett vergessen.
	//
	// buildPrecipitationPieces: n = getTopY(...); o = max(camY-r, n); p = max(camY+r, n).
	// getTopY liefert in geschlossenen Arenen die DECKE -> o==p==Decke -> kein Schnee.
	// Wir redirecten getTopY und geben einen Wert WEIT unter dem Spieler zurueck
	// (world.getBottomY()). Dadurch wird o=camY-r, p=camY+r: der Schnee faellt
	// IMMER in einem Fenster um den Spieler herum, egal was ueber ihm ist
	// (Barrier, Glas, Stein, hoch, niedrig - alles). Kein Block-Lookup, keine
	// Hoehengrenze, keine Fragilitaet.
	//
	// Trade-off: schneit auch unter echten Daechern. Fuer PvP/Arenen gewollt;
	// per Option (snowThroughBarriers) abschaltbar.
	@Redirect(
		method = "buildPrecipitationPieces",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTopY(Lnet/minecraft/world/Heightmap$Type;II)I")
	)
	private int perpetualsnow$redirectTopY(World world, Heightmap.Type type, int m, int l) {
		int realTop = world.getTopY(type, m, l);
		if (!PerpetualSnow.enabled || !PerpetualSnow.snowThroughBarriers || !active(world)) {
			return realTop;
		}
		// Weit unter dem Spieler -> Schnee-Fenster wird camY +/- Radius, Decke egal.
		return world.getBottomY();
	}

	// --- 2) immer SNOW ------------------------------------------------
	@ModifyReturnValue(method = "getPrecipitationAt", at = @At("RETURN"))
	private Biome.Precipitation perpetualsnow$forceSnow(Biome.Precipitation original, World world, BlockPos pos) {
		if (!active(world)) {
			return original;
		}
		return Biome.Precipitation.SNOW;
	}

	// --- 3) Fallgeschwindigkeit + seitlicher Drift --------------------
	// createSnowPiece liefert ein unveraenderliches Piece-Record. Wir bauen ein
	// neues mit skaliertem uOffset (Drift) und vOffset (Fall). Mod inaktiv ->
	// Original unveraendert. World ist hier nicht im Scope, daher nur globaler
	// enabled-Check (Dimension wird bereits in build/getPrecipitation gefiltert:
	// ist die Dimension aus, gibt es keine snowPieces, also kein Aufruf hierher).
	@ModifyReturnValue(method = "createSnowPiece", at = @At("RETURN"))
	private WeatherRendering.Piece perpetualsnow$shapeSnow(WeatherRendering.Piece p) {
		if (!PerpetualSnow.enabled) {
			return p;
		}
		float drift = PerpetualSnow.clamp(PerpetualSnow.drift, 0.0F, 3.0F);
		float fall  = PerpetualSnow.clamp(PerpetualSnow.fallSpeed, 0.2F, 3.0F);
		// uOffset skaliert den horizontalen Versatz (Drift); vOffset die vertikale
		// Textur-Verschiebung pro Frame (Fallbewegung).
		float newU = p.uOffset() * drift;
		float newV = p.vOffset() * fall;

		// Volle Beleuchtung: unter Barrier-Decken ist das Skylight reduziert, der
		// Schnee wuerde dunkel/unsichtbar. pack(15,15) = volle Helligkeit.
		int light = p.lightCoords();
		if (PerpetualSnow.fullBright) {
			light = net.minecraft.client.render.LightmapTextureManager.pack(15, 15);
		}

		if (newU == p.uOffset() && newV == p.vOffset() && light == p.lightCoords()) {
			return p;
		}
		return new WeatherRendering.Piece(p.x(), p.z(), p.bottomY(), p.topY(), newU, newV, light);
	}

	// --- 4) Deckkraft -------------------------------------------------
	// In renderPieces ist 'j' der Alpha-Float, der an ColorHelper.getWhite(j)
	// geht. Wir multiplizieren ihn mit der Deckkraft, bevor die Farbe gebaut wird.
	// Greift fuer Regen UND Schnee-Pieces; da wir nur Schnee erzeugen, praktisch
	// nur Schnee. Mod inaktiv -> unveraendert.
	@ModifyExpressionValue(
		method = "renderPieces",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ColorHelper;getWhite(F)I")
	)
	private int perpetualsnow$opacity(int originalColor) {
		if (!PerpetualSnow.enabled) {
			return originalColor;
		}
		float op = PerpetualSnow.clamp(PerpetualSnow.opacity, 0.1F, 1.0F);
		if (op >= 1.0F) {
			return originalColor;
		}
		// ARGB: Alpha ist das oberste Byte. Nur Alpha skalieren.
		int a = (originalColor >>> 24) & 0xFF;
		a = Math.round(a * op);
		if (a < 0) a = 0; else if (a > 255) a = 255;
		return (a << 24) | (originalColor & 0x00FFFFFF);
	}

	// --- 5) Flockengroesse --------------------------------------------
	// Die halbe Flockenbreite entsteht in renderPieces als:
	//     float m = NORMAL_LINE_DX[l] / 2.0F;
	//     float n = NORMAL_LINE_DZ[l] / 2.0F;
	// Wir fangen BEIDE Divisionsergebnisse ab (das sind die einzigen beiden
	// " / 2.0F "-Float-Divisionen in der Methode) und skalieren sie mit der
	// gewuenschten Flockengroesse. @ModifyExpressionValue auf die Division ist
	// robuster als @ModifyVariable auf benannte Locals, weil es nicht an der
	// Variablen-Tabelle haengt.
	//
	// UNSICHER / BITTE IM SPIEL PRUEFEN: Wenn mehr als diese zwei Float-"/2"-
	// Divisionen existieren (Decompiler kann optimieren), trifft es evtl. die
	// falsche. Reagiert die Groesse nicht oder meldet der Build eine mehrdeutige
	// Injection -> das ist der Verdaechtige, dann stelle ich auf @Redirect des
	// Array-Loads mit Index um.
	@ModifyExpressionValue(
		method = "renderPieces",
		at = @At(value = "CONSTANT", args = "floatValue=2.0", ordinal = 0),
		require = 0
	)
	private float perpetualsnow$sizeDivisor1(float original) {
		return perpetualsnow$scaleDivisor(original);
	}

	@ModifyExpressionValue(
		method = "renderPieces",
		at = @At(value = "CONSTANT", args = "floatValue=2.0", ordinal = 1),
		require = 0
	)
	private float perpetualsnow$sizeDivisor2(float original) {
		return perpetualsnow$scaleDivisor(original);
	}

	// Groessere Flocke = kleinerer Divisor (Breite = DX / divisor).
	private static float perpetualsnow$scaleDivisor(float originalDivisor) {
		if (!PerpetualSnow.enabled) {
			return originalDivisor;
		}
		float size = PerpetualSnow.clamp(PerpetualSnow.flakeSize, 0.2F, 3.0F);
		if (size == 1.0F) {
			return originalDivisor;
		}
		return originalDivisor / size;
	}

	// -----------------------------------------------------------------
	private static boolean active(World world) {
		if (!PerpetualSnow.enabled || world == null) {
			return false;
		}
		var key = world.getRegistryKey();
		if (key == World.OVERWORLD) return PerpetualSnow.overworld;
		if (key == World.NETHER)    return PerpetualSnow.nether;
		if (key == World.END)       return PerpetualSnow.end;
		return PerpetualSnow.overworld; // modded/unbekannt: wie Overworld
	}
}
