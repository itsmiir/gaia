package com.miir.gaia.gen.artemis;

import com.miir.gaia.gen.WorldGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.function.Function;

/**
 * artemis paints the surface onto the world after it's been built
 */
public class Artemis {
    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.getDefaultState();
    private static final BlockState DIRT = Blocks.DIRT.getDefaultState();
    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    private static final BlockState SAND = Blocks.SAND.getDefaultState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();
    public static final Function<SurfaceBuilderContext, BlockState> SURFACE_RULE = ctx -> {
        BlockState state = ctx.state();
        BlockState above = ctx.above();
        int y = ctx.pos().getY();
        int depth = ctx.depth();
        if (state.getFluidState().isOf(Fluids.EMPTY) && ctx.surfaceHeight() < WorldGenerator.SEA_LEVEL + 3) { // block is at or below sea level plus a small offset
            switch (depth) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    return SAND;
                case 6:
                case 7:
                    return SANDSTONE;
                default:
            }
        }
        if (above.getFluidState().isOf(Fluids.EMPTY)) { // block is not below water
            if (state.getFluidState().isOf(Fluids.EMPTY)) { // block is not water and not below water
                switch (depth) {
                    case 1:
                    case 2:
                        return GRASS_BLOCK;
                    case 3:
                    case 4:
                    case 5:
                        return DIRT;
                    default:
                }
            }
        }
        return AIR;
    };


    public static void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk, Function<SurfaceBuilderContext, BlockState> surfaceRule) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        BlockState above = Blocks.AIR.getDefaultState();
        for (int x = 0; x < 16; x++) {
            pos.setX(x);
            for (int z = 0; z < 16; z++) {
                pos.setZ(z);
                int depth = 0;
                int surfaceHeight = 0;
                for (int y = WorldGenerator.MAX_HEIGHT; y > 0; y--) {
                    pos.setY(y);
                    BlockState state = chunk.getBlockState(pos);
                    if (!state.isOf(Blocks.AIR)) {
                        depth += 1;
                        if (depth == 1 || depth == 2) surfaceHeight = y;
                        state = surfaceRule.apply(new SurfaceBuilderContext(pos, surfaceHeight, depth, state, above, WorldGenerator.RANDOM));
                        if (!state.isOf(Blocks.AIR)) chunk.setBlockState(pos, state, false);
                        above = state;
                    }
                }
            }
        }
    }
}
