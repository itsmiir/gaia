package com.miir.gaia.gen.volos;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.vulcan.Vulcan;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/**
 * after vulcan determines the macroscopic shape of the terrain, volos creates its final shape, on the meter scale.
 */
public abstract class Volos {
    public static final int SEA_LEVEL = WorldGenerator.WORLDGEN_BASE_HEIGHT /2 - 64;
    public static BlockState sampleNoise(int x, int y, int z) {
        int height = getHeight(x, z);
        if (y <= height) {
            return null;
        } else if (y <= SEA_LEVEL) {
            return Blocks.WATER.getDefaultState();
        }
        return GaiaChunkGenerator.AIR;
    }
    public static int getHeight(int x, int z) {
        int modX = Vulcan.blockToAtlasCoord(x);
        int modZ = Vulcan.blockToAtlasCoord(z);
        float elevation = Vulcan.MAP[modX][modZ].getValue();
        return Math.round(elevation * WorldGenerator.WORLDGEN_BASE_HEIGHT) - 64;
    }

}
