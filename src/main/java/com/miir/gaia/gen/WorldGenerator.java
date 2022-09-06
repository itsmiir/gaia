package com.miir.gaia.gen;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.visiwa.AtlasPoint;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.Random;

public abstract class WorldGenerator {
    public static final BlockState DEFAULT_BLOCK = Blocks.STONE.getDefaultState();
    public static final int WORLDGEN_BASE_HEIGHT = 256;
    public static final int WORLD_HEIGHT = 384;
    public static final int MIN_Y = -64;
    public static long SEED;
    private static Random RANDOM;
    private static SimplexNoiseSampler simplex;
    public static boolean INITIALIZED = false;

    /**
     * the world border is generated as a square with side length 2*WORLD_RADIUS. the actual playable area is its inscribed circle.
     * a radius of 400,000 m -> π*4000000² = 502.655 Gm²; compare to earth's surface area of 510.072 Gm². a more accurate
     * approximation would be 402,940 m -> 510.0710 Gm²
     */
    public static final int WORLD_RADIUS = 400000;

    /**
     * the world surface height is stored as a float from 0 to 1; mapped to these values. earth's surface deviates by
     * about ±10 km ASL in each direction, which is super convenient
     */
    public static final int MAX_HEIGHT = 10000;
    public static final int SEA_LEVEL = 0;
    public static final int MIN_SEAFLOOR_HEIGHT = -10000;
    //    this value is used to determine the starting seafloor height
    public static final int MEAN_SEAFLOOR_DEPTH = -5000;


    // noise settings used by visiwa
    public static final int ATLAS_WIDTH = 512;
    public static final int HEIGHTMAP_OCTAVES = 6;
    public static final int SCALE_FACTOR = 2;
    public static final int ATLAS_AREA = (int) ((ATLAS_WIDTH/2f)*(ATLAS_WIDTH/2f)*Math.PI);





    public static void initialize(long seed) {
        SEED = seed;
        RANDOM = new Random(seed);
        simplex = new SimplexNoiseSampler(new CheckedRandom(SEED));
        INITIALIZED = true;
        Visiwa.MAP = new AtlasPoint[ATLAS_WIDTH][ATLAS_WIDTH];
    }

    public static double sampleSimplex(double x, double y) {
        if (!INITIALIZED) {
            throw new IllegalStateException("world generator uninitialized!");
        } else {
            return simplex.sample(x, y);
        }
    }
    public static double random() {
        if (!INITIALIZED) {
            throw new IllegalStateException("world generator uninitialized!");
        } else {
            return RANDOM.nextDouble();
        }
    }

    public static boolean isValidAtlasPos(int x, int y) {
        return baseHeight(x / ((float) ATLAS_WIDTH), y / ((float) ATLAS_WIDTH)) >= 0 && x < ATLAS_WIDTH && y < ATLAS_WIDTH;
    }
    public static boolean isValidAtlasPos(Point p) {
        return isValidAtlasPos(p.x, p.y);
    }

    public static float baseHeight(float x, float y) {
        float xMod = x*2-1;
        float yMod = y*2-1;
        return (Math.sqrt(Math.pow(xMod, 2) + Math.pow(yMod, 2)) > 1) ? -1 : 0.1f;
    }

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, Gaia.id("gaia"), GaiaChunkGenerator.CODEC);
    }
}
