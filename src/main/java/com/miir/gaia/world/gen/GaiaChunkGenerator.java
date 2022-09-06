package com.miir.gaia.world.gen;

import com.google.common.collect.Sets;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.volos.Volos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class GaiaChunkGenerator extends ChunkGenerator {
    public static final BlockState AIR = Blocks.AIR.getDefaultState();
    private final ChunkGeneratorSettings settings;
    private final AquiferSampler.FluidLevelSampler fluidLevelSampler;
    private Registry<Biome> biomes;
    private Registry<StructureSet> structureSets;

    public static final Codec<GaiaChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.LONG.fieldOf("seed").forGetter(GaiaChunkGenerator::getSeed),
                    RegistryOps.createRegistryCodec(Registry.BIOME_KEY).forGetter(GaiaChunkGenerator::getBiomes),
                    RegistryOps.createRegistryCodec(Registry.STRUCTURE_SET_KEY).forGetter(GaiaChunkGenerator::getStructureSets))
                    .apply(instance, GaiaChunkGenerator::new));
    public long seed = 0;

    public GaiaChunkGenerator(long seed, Registry<Biome> biomes, Registry<StructureSet> structures) {
        super(structures, Optional.empty(), MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(biomes));
        this.seed = seed;
        ChunkGeneratorSettings chunkGeneratorSettings = BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD).value();
        this.settings = chunkGeneratorSettings;
        int i = chunkGeneratorSettings.seaLevel();
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, chunkGeneratorSettings.defaultFluid());
        this.fluidLevelSampler = (x, y, z) -> {
            if (y < Math.min(-54, i)) {
                return fluidLevel;
            }
            return fluidLevel2;
        };
    }

    public GaiaChunkGenerator(Registry<Biome> biomes, Registry<StructureSet> structures) {
        super(structures, Optional.empty(), MultiNoiseBiomeSource.Preset.OVERWORLD.getBiomeSource(biomes));
        ChunkGeneratorSettings chunkGeneratorSettings = BuiltinRegistries.CHUNK_GENERATOR_SETTINGS.getOrCreateEntry(ChunkGeneratorSettings.OVERWORLD).value();
        this.settings = chunkGeneratorSettings;
        int i = chunkGeneratorSettings.seaLevel();
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
        AquiferSampler.FluidLevel fluidLevel2 = new AquiferSampler.FluidLevel(i, chunkGeneratorSettings.defaultFluid());
        this.fluidLevelSampler = (x, y, z) -> {
            if (y < Math.min(-54, i)) {
                return fluidLevel;
            }
            return fluidLevel2;
        };
    }

    public long getSeed() {
        return seed;
    }
    public Registry<StructureSet> getStructureSets() {
        return structureSets;
    }
    public Registry<Biome> getBiomes() {
        return biomes;
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {

    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(Registry<Biome> biomeRegistry, Executor executor, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return super.populateBiomes(biomeRegistry, executor, noiseConfig, blender, structureAccessor, chunk);
    }

    @Override
    public void populateEntities(ChunkRegion region) {

    }

    @Override
    public int getWorldHeight() {
        return WorldGenerator.WORLD_HEIGHT;
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        GenerationShapeConfig generationShapeConfig = this.settings.generationShapeConfig().trimHeight(chunk.getHeightLimitView());
        int i = generationShapeConfig.minimumY();
        int j = MathHelper.floorDiv(i, generationShapeConfig.verticalBlockSize());
        int k = MathHelper.floorDiv(generationShapeConfig.height(), generationShapeConfig.verticalBlockSize());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunk);
        }
        int l = chunk.getSectionIndex(k * generationShapeConfig.verticalBlockSize() - 1 + i);
        int m = chunk.getSectionIndex(i);
        HashSet<ChunkSection> set = Sets.newHashSet();
        for (int n = l; n >= m; --n) {
            ChunkSection chunkSection = chunk.getSection(n);
            chunkSection.lock();
            set.add(chunkSection);
        }
        return CompletableFuture.supplyAsync(Util.debugSupplier("wgen_fill_noise", () -> this.populateNoise(blender, structureAccessor, noiseConfig, chunk, j, k)), Util.getMainWorkerExecutor()).whenCompleteAsync((chunk2, throwable) -> {
            for (ChunkSection chunkSection : set) {
                chunkSection.unlock();
            }
        }, executor);
    }
    public final Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minCubeY, int cellHeight) {
//        get the noise sampler
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(chunk1 -> {
            return ChunkNoiseSampler.create(
                    chunk1,
                    noiseConfig,
                    StructureWeightSampler.createStructureWeightSampler(structureAccessor, chunk1.getPos()),
                    this.settings,
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

    @Override
    public int getSeaLevel() {
        return WorldGenerator.SEA_LEVEL;
    }

    @Override
    public int getMinimumY() {
        return WorldGenerator.MIN_Y;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return Volos.getHeight(x, z);
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[getWorldHeight()];
        int h = Volos.getHeight(x, z);
        for (int i = 0; i < getWorldHeight(); i++) {
            if (i <= h) {
                states[i] = settings.defaultBlock();
            } else {
                states[i] = AIR;
            }
        }
        return new VerticalBlockSample(getWorldHeight(), states);
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
