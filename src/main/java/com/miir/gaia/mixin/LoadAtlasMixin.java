package com.miir.gaia.mixin;

import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.world.gen.GaiaChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(MinecraftServer.class)
public abstract class LoadAtlasMixin {
    @Shadow @Final protected SaveProperties saveProperties;

    @Shadow public abstract Path getSavePath(WorldSavePath worldSavePath);

    /**
     * initializes the world generator and loads the atlas, or creates one if it doesn't exist
     */
    @Inject(method = "createWorlds", at = @At("HEAD"))
    private void initAtlas(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        ChunkGenerator generator = this.saveProperties.getGeneratorOptions().getChunkGenerator();
        if (generator instanceof GaiaChunkGenerator) WorldGenerator.initialize(
                this.saveProperties.getGeneratorOptions().getSeed(),
                this.getSavePath(WorldSavePath.GENERATED).toString()
        );
    }
}
