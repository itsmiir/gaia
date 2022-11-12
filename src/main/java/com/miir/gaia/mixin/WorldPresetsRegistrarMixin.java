package com.miir.gaia.mixin;

import com.miir.gaia.Gaia;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldPresets.Registrar.class)
public abstract class WorldPresetsRegistrarMixin {

    @Shadow protected abstract RegistryEntry<WorldPreset> register(RegistryKey<WorldPreset> key, DimensionOptions dimensionOptions);
    @Shadow @Final private Registry<StructureSet> structureSetRegistry;
    @Shadow @Final private Registry<Biome> biomeRegistry;

    @Inject(method = "initAndGetDefault", at = @At("RETURN"))
    private void addGaiaPreset(CallbackInfoReturnable<RegistryEntry<WorldPreset>> cir) {
        this.register(
                Gaia.GAIA_PRESET_KEY,
                new DimensionOptions(
                        BuiltinRegistries.DIMENSION_TYPE.entryOf(
                                RegistryKey.of(
                                        Registry.DIMENSION_TYPE_KEY,
                                        Gaia.id("gaia"))),
                        new GaiaChunkGenerator(
                                biomeRegistry,
                                structureSetRegistry)));
        // todo: implement config screen
//        LevelScreenProvider.WORLD_PRESET_TO_SCREEN_PROVIDER.put(Optional.of(Gaia.GAIA_PRESET_KEY), new );
    }
}
