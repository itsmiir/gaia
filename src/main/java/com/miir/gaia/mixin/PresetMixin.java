package com.miir.gaia.mixin;

import com.miir.gaia.Gaia;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldPresets.Registrar.class)
public abstract class PresetMixin {
    @Shadow protected abstract RegistryEntry<WorldPreset> register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);

    @Shadow @Final private Registry<StructureSet> structureSetRegistry;
    @Shadow @Final private Registry<Biome> biomeRegistry;

    @Shadow protected abstract DimensionOptions createOverworldOptions(ChunkGenerator chunkGenerator);



    // defining our registry key. this key provides an Identifier for our preset, that we can use for our lang files and data elements.
    private static final RegistryKey<WorldPreset> GAIA = RegistryKey.of(Registry.WORLD_PRESET_KEY, Gaia.id("gaia"));

    @Inject(method = "initAndGetDefault", at = @At("RETURN"))
    private void addPresets(CallbackInfoReturnable<RegistryEntry<WorldPreset>> cir) {
        // the register() method is shadowed from the target class
        this.register(
                GAIA, new DimensionOptions(
                        BuiltinRegistries.DIMENSION_TYPE.entryOf(
                                RegistryKey.of(
                                        Registry.DIMENSION_TYPE_KEY,
                                        Gaia.id("gaia"))),
                        new GaiaChunkGenerator(
                                biomeRegistry,
                                structureSetRegistry)));
    }
}
