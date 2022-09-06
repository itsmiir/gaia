package com.miir.gaia.gen.vulcan;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

/**
 * after visiwa determines the macroscopic shape of the terrain, vulcan creates its final shape, on the meter scale.
 */
public abstract class Vulcan {
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
        int modX = Visiwa.blockToAtlasCoord(x);
        int modZ = Visiwa.blockToAtlasCoord(z);
        float elevation = Visiwa.MAP[modX][modZ].getValue();
        return Math.round(elevation * WorldGenerator.WORLDGEN_BASE_HEIGHT) - 64;
    }

}
