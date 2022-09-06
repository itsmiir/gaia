package com.miir.gaia;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.vulcan.Vulcan;
import com.miir.gaia.vis.MapPrinter;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Gaia implements ModInitializer {
	public static final String MOD_ID = "gaia";
	public static final Logger LOGGER = LoggerFactory.getLogger("gaia");

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		Registry.register(Registry.CHUNK_GENERATOR, Gaia.id("gaia"), GaiaChunkGenerator.CODEC);
//		long seed = 379541375374700L;
		long seed = System.nanoTime();
		WorldGenerator.initialize(seed);
		Vulcan.build();
		MapPrinter.printAtlas("map", Vulcan::colorWithMarkers);
//		throw new IllegalStateException(Long.toString(seed));
	}

}
