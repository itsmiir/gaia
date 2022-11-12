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
    public static BlockState sampleNoise(int x, int y, int z) {
        float height = getHeight(x, z);
        if (y <= height) {
            return null;
        } else if (y <= WorldGenerator.SEA_LEVEL) {
            return Blocks.WATER.getDefaultState();
        }
        return GaiaChunkGenerator.AIR;
    }

    public static float getHeight(int x, int z) {
        if (!WorldGenerator.isInsideWorld(x, z)) return 0;
        int modX = Visiwa.blockToAtlasCoord(x);
        int modZ = Visiwa.blockToAtlasCoord(z);
        if (modX == -1 || modZ == -1) return 0;
        float elevation = (float) Visiwa.lerpElevation(modX, modZ, x, z);
        elevation = Visiwa.scaleElevation(elevation);
        return elevation + sampleSurfaceNoise(x, z);
    }

    private static float sampleSurfaceNoise(double x, double z) {
        float h = 0;
        x /= 512f;
        z /= 512f;
        for (int n = 0; n < WorldGenerator.HEIGHTMAP_OCTAVES-1; n++) {
            h += (WorldGenerator.sampleSimplex(
                    x * 2 * (Math.pow(2, n)),
                    z * 2 * (Math.pow(2, n)))
            ) / (4*Math.pow(2, n));
            h -= (WorldGenerator.sampleSimplex(
                    (1 - x) * (Math.pow(2, n)),
                    (1 - z) * (Math.pow(2, n)))
                    - 1) / (10 * Math.pow(2, n));
        }
        return heightTransform(h);
    }

    private static float heightTransform(float h) {
        return h;
    }

}
