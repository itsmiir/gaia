package com.miir.gaia;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.vulcan.Vulcan;
import com.miir.gaia.vis.MapPrinter;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Gaia implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("gaia");

	@Override
	public void onInitialize() {
//		long seed = 379541375374700L;
		long seed = System.nanoTime();

		WorldGenerator.initialize(seed);
		Vulcan.build();
		MapPrinter.printAtlas("map", Vulcan::colorWithMarkers);
		throw new IllegalStateException(Long.toString(seed));
	}
}
