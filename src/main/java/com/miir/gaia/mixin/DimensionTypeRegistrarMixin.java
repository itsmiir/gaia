package com.miir.gaia.mixin;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.WorldGenerator;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypeRegistrar;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(DimensionTypeRegistrar.class)
public class DimensionTypeRegistrarMixin {
    @Inject(
            method = "initAndGetDefault",
            at = @At("HEAD")
    )
    private static void addGaiaType(Registry<DimensionType> registry, CallbackInfoReturnable<RegistryEntry<DimensionType>> cir) {
        BuiltinRegistries.add(
                registry,
                RegistryKey.of(Registry.DIMENSION_TYPE_KEY, Gaia.id("gaia")),
                new DimensionType(
                        OptionalLong.empty(),
                        true, false, false,
                        true, 1.0, true, false,
                        WorldGenerator.MIN_HEIGHT,
                        WorldGenerator.MAX_HEIGHT - WorldGenerator.MIN_HEIGHT,
                        WorldGenerator.MAX_HEIGHT - WorldGenerator.MIN_HEIGHT,
                        BlockTags.INFINIBURN_OVERWORLD,
                        DimensionTypes.OVERWORLD_ID,
                        0.0f,
                        new DimensionType.MonsterSettings(
                                false, true, UniformIntProvider.create(0, 7), 0)
                ));
    }
}
