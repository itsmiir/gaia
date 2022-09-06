package com.miir.gaia.mixin;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.volos.Volos;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
    @Shadow @Final protected RegistryEntry<ChunkGeneratorSettings> settings;

    @Shadow @Final private AquiferSampler.FluidLevelSampler fluidLevelSampler;

    @Shadow @Final private static BlockState AIR;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minCubeY, int cellHeight) {
        //        get the noise sampler
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(chunk1 -> {
            return ChunkNoiseSampler.create(
                    chunk1,
                    noiseConfig,
                    StructureWeightSampler.createStructureWeightSampler(structureAccessor, chunk1.getPos()),
                    this.settings.value(),
                    this.fluidLevelSampler,
                    blender
            );});
//        get the chunk heightmaps for the land and sea, so we can update them when we place each block
        Heightmap oceanFloorHeightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap worldSurfaceHeightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();
        AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartNoise(); // startInterpolation
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int horizontalBlockSize = chunkNoiseSampler.getHorizontalBlockSize(); // horizontal and vertical block size
        int verticalBlockSize = chunkNoiseSampler.getVerticalBlockSize();
        int horizontalX = 16 / horizontalBlockSize; // number of sub-chunks in the chunk (16 = the number of blocks across a chunk)
        int horizontalZ = 16 / horizontalBlockSize;

//        iterate through each 4x4 section of the chunk
        for (int chunkCubeX = 0; chunkCubeX < horizontalX; ++chunkCubeX) {
            chunkNoiseSampler.sampleEndNoise(chunkCubeX);
            for (int chunkCubeZ = 0; chunkCubeZ < horizontalZ; ++chunkCubeZ) {
//                get the top section of the chunk
                ChunkSection chunkSection = chunk.getSection(chunk.countVerticalSections() - 1);
                for (int chunkCubeY = cellHeight - 1; chunkCubeY >= 0; --chunkCubeY) {
                    chunkNoiseSampler.sampleNoiseCorners(chunkCubeY, chunkCubeZ);

//                    get local and absolute y coords
                    for (int deltaY = verticalBlockSize - 1; deltaY >= 0; --deltaY) {
                        int absoluteY = (minCubeY + chunkCubeY) * verticalBlockSize + deltaY;
                        int localY = absoluteY & 0xF;
                        int chunkSectionIndex = chunk.getSectionIndex(absoluteY);

//                        ensure that the working chunk section is the same as the absoluteY one
                        if (chunk.getSectionIndex(chunkSection.getYOffset()) != chunkSectionIndex) {
                            chunkSection = chunk.getSection(chunkSectionIndex);
                        }

//                        the percentage of the way through the top of the chunk we are

//                        get local and absolute x coords
                        for (int x = 0; x < horizontalBlockSize; ++x) {
                            int absoluteX = chunkStartX + chunkCubeX * horizontalBlockSize + x;
                            int localX = absoluteX & 0xF;

//                            the percentage of the way through the chunk we are (x direction)

//                            get local and absolute z coords
                            for (int z = 0; z < horizontalBlockSize; ++z) {
                                int absoluteZ = chunkStartZ + chunkCubeZ * horizontalBlockSize + z;
                                int localZ = absoluteZ & 0xF;

//                                sample the blockstate at the current sample point
                                BlockState blockState = Volos.sampleNoise(absoluteX, absoluteY, absoluteZ);
                                if (blockState == null) {
                                    blockState = WorldGenerator.DEFAULT_BLOCK;
                                }
//                                if the blockstate is air or the world is debug and the current chunk is outside a certain area, no lighting, water, or heightmap updates needed
                                if (blockState == AIR || SharedConstants.isOutsideGenerationArea(chunk.getPos())) continue;

//                                add the light source to the chunk if it's a ProtoChunk (something to do with lighting)
                                if (blockState.getLuminance() != 0 && chunk instanceof ProtoChunk) {
                                    mutablePos.set(absoluteX, absoluteY, absoluteZ);
                                    ((ProtoChunk) chunk).addLightSource(mutablePos);
                                }

//                                set the blockstate to the blockstate we defined
                                chunkSection.setBlockState(localX, localY, localZ, blockState, false);

//                                update the heightmaps
                                oceanFloorHeightmap.trackUpdate(localX, absoluteY, localZ, blockState);
                                worldSurfaceHeightmap.trackUpdate(localX, absoluteY, localZ, blockState);

//                                if there's no fluid at this blockpos or the aquifer placer says it's okay, no post-processing needed
                                if (!aquiferSampler.needsFluidTick() || blockState.getFluidState().isEmpty())
                                    continue;
                                mutablePos.set(absoluteX, absoluteY, absoluteZ);
                                chunk.markBlockForPostProcessing(mutablePos);
                            }
                        }
                    }
                }
            }
//            swap the startNoiseBuffer and the endNoiseBuffer
            chunkNoiseSampler.swapBuffers();
        }
        chunkNoiseSampler.stopInterpolation(); // mark as finished
        return chunk;
    }
}
