package com.miir.gaia.gen.artemis;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public record SurfaceBuilderContext(BlockPos pos, int surfaceHeight, int depth, BlockState state, BlockState above, Random random){}
