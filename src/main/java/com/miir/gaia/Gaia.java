package com.miir.gaia;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.vis.MapPrinter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gaia implements ModInitializer {
	public static final String MOD_ID = "gaia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String ATLAS_PATH = "\\atlas.bin";

    public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WorldGenerator.register();
//		long seed = 379541375374700L;
//		long seed = System.nanoTime();
//		MapPrinter.printAtlas("map", Visiwa::colorWithMarkers);
//		throw new IllegalStateException(Long.toString(seed));
	}

}
