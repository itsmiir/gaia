package com.miir.gaia.gen;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.visiwa.AtlasPoint;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.serialization.GaiaSerializer;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.Random;

public abstract class WorldGenerator {
    public static final BlockState DEFAULT_BLOCK = Blocks.STONE.getDefaultState();
    public static final int WORLDGEN_BASE_HEIGHT = 256;
    public static final BlockState DEFAULT_FLUID = Blocks.WATER.getDefaultState();
    public static final int MAX_PLATEAU_Y = 1000;
    public static final int LAVA_LEVEL = -100;
    public static boolean SHOULD_GENERATE = false;
    public static long SEED;
    public static Random RANDOM;
    private static SimplexNoiseSampler simplex;
    public static boolean INITIALIZED = false;

    /**
     * the world border is generated as a square with side length 2*WORLD_RADIUS. the actual playable area is its inscribed circle.
     * a radius of 400,000 m -> π*4000000² = 502.655 Gm²; compare to earth's surface area of 510.072 Gm². a more accurate
     * approximation would be 402,940 m -> 510.0710 Gm²
     */
    public static final int WORLD_RADIUS = 1024*8;

    /**
     * the world surface height is stored as a float from 0 to 1; mapped to these values. earth's surface deviates by
     * almost exactly ±10 km ASL in each direction, which is super convenient, but also really, really big. until i implement some
     * sort of cubic chunks, there's no way your computer will handle 16x16x20000 chunks (with maybe 100 blocks on each
     * end shaved off for caves/mountain builds). for those keeping track at home 20000 m-tall chunks are about 50 times
     * as big as the vanilla chunks.
     */
    public static final int MAX_HEIGHT = 1024;
    public static final int SEA_LEVEL = WORLDGEN_BASE_HEIGHT /2 - 64;
    public static final int MIN_HEIGHT = -MAX_HEIGHT;
    //    this value is used to determine the starting seafloor height
    public static final int MEAN_SEAFLOOR_DEPTH = MIN_HEIGHT / 2;


    // noise settings used by visiwa
    public static final int ATLAS_WIDTH = 512;
    public static final int HEIGHTMAP_OCTAVES = 6;
    public static final int ATLAS_AREA = (int) ((ATLAS_WIDTH/2f)*(ATLAS_WIDTH/2f)*Math.PI);
    public static final int SCALE_FACTOR = WORLD_RADIUS*2 / ATLAS_WIDTH;

    public static AtlasPoint[][] MAP;

    public static void initialize(long seed, String path) {
        SEED = seed;
        RANDOM = new Random(seed);
        simplex = new SimplexNoiseSampler(new CheckedRandom(SEED));
        INITIALIZED = true;
        MAP = GaiaSerializer.readAtlas(path);
        if (SHOULD_GENERATE) {
            Visiwa.build();
            if (!GaiaSerializer.writeAtlas(path)) {
                Gaia.LOGGER.error("could not save atlas to level directory!");
            }
            SHOULD_GENERATE = false;
        }
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
    public static boolean isInsideWorld(int x, int z) {
        return baseHeight(x / ((float) WORLD_RADIUS), z / (float) WORLD_RADIUS) < 0;
//        int atlasX = Visiwa.blockToAtlasCoord(x);
//        int atlasZ = Visiwa.blockToAtlasCoord(z);
//        if (atlasX == -1 || atlasZ == -1) return false;
//        else return isValidAtlasPos(atlasX, atlasZ);
    }
    public static boolean isValidChunk(ChunkPos pos) {
        return isInsideWorld(pos.x / 16, pos.z / 16);
    }

    /**
     * this function takes in a uv coordinate from an (assumed to be square) map, and uses that to generate the circle shape that the world follows
     */
    public static float baseHeight(float x, float y) {
        float xMod = x*2-1;
        float yMod = y*2-1;
        return (Math.sqrt(Math.pow(xMod, 2) + Math.pow(yMod, 2)) > 1) ? -1 : 0.1f;
    }

    public static float getElevation(int x, int z) {
        int xx = x < WorldGenerator.ATLAS_WIDTH ? x : WorldGenerator.ATLAS_WIDTH-1;
        int zz = z < WorldGenerator.ATLAS_WIDTH ? z : WorldGenerator.ATLAS_WIDTH-1;
        return MAP[xx][zz].getElevation();
    }

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, Gaia.id("gaia"), GaiaChunkGenerator.CODEC);
    }
}
