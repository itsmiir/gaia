package com.miir.gaia;

import com.miir.gaia.debug.Debug;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.vis.MapPrinter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.WorldPreset;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class Gaia implements ModInitializer {

	/**
	 * todo:
	 * -import visiwa code ☺
	 * -get world generator to build from atlas ☺
	 * -create preset and chunk generator ☺
	 * -implement per-world serialization ☺
	 * -add option to generate map ingame ☺
	 * -lerped noise ☺
	 * -small-scale noise variations ☺
	 * -mid-scale noise variations (hilly/mountainous regions)
	 * -implement humidity and temperature maps
	 * -biomes + surface builder
	 * -geographic POIs (monolithic mountains and such)
	 * -3d noise
	 * -cubic chunks impl
	 */

	public static final String MOD_ID = "gaia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String ATLAS_PATH = "\\atlas.bin";
	public static final RegistryKey<WorldPreset> GAIA_PRESET_KEY = RegistryKey.of(Registry.WORLD_PRESET_KEY, Gaia.id("gaia"));

	public static @NotNull Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WorldGenerator.register();
		Debug.register();
//		MapPrinter.printAtlas("map", Visiwa::colorWithMarkers);
//		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
//				((CommandDispatcher<ServerCommandSource>) dispatcher).register(literal("atlas")
//						.then(literal("this")
//								.requires(source -> ((ServerCommandSource) source).hasPermissionLevel(4))
//								.executes(context -> {
//									if (context.getSource() instanceof ServerCommandSource source) {
//										Debug.generateWorldMap(source.getWorld().getSeed(), true);
//										source.sendFeedback(Text.literal("generated this world's map!"), true);
//										return Command.SINGLE_SUCCESS;
//									}
//									return 0;
//								})
//						)
//				)
//		);
	}
}
