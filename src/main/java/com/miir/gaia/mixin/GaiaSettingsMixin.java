package com.miir.gaia.mixin;

import com.miir.gaia.Gaia;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGeneratorSettings.class)
public class GaiaSettingsMixin {
    @Invoker
    static RegistryEntry<ChunkGeneratorSettings> callRegister(Registry<ChunkGeneratorSettings> registry, RegistryKey<ChunkGeneratorSettings> key, ChunkGeneratorSettings chunkGeneratorSettings) {
        throw new UnsupportedOperationException();
    }
    @Invoker
    static ChunkGeneratorSettings callCreateSurfaceSettings(boolean amplified, boolean largeBiomes) {
        throw new UnsupportedOperationException();
    }

    @Inject(at = @At("HEAD"), method = "initAndGetDefault")
    private static void mixin(Registry<ChunkGeneratorSettings> registry, CallbackInfoReturnable<RegistryEntry<ChunkGeneratorSettings>> cir) {
        callRegister(registry, RegistryKey.of(Registry.CHUNK_GENERATOR_SETTINGS_KEY, Gaia.id("gaia")), callCreateSurfaceSettings(false, false));
    }
}
