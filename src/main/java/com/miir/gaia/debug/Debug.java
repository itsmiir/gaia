package com.miir.gaia.debug;

import com.miir.gaia.Gaia;
import com.miir.gaia.gen.WorldGenerator;
import com.miir.gaia.gen.visiwa.Visiwa;
import com.miir.gaia.vis.MapPrinter;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class Debug {
    public static final Item WORLD_WAND = new WorldWandItem(new FabricItemSettings());
    public static void register() {
        Registry.register(Registry.ITEM, Gaia.id("world_wand"), WORLD_WAND);
    }
    public static void generateWorldMap(long seed, boolean useSeed) {
        if (!useSeed) WorldGenerator.SEED = System.currentTimeMillis();
        Visiwa.build();
        WorldGenerator.SEED = seed;
        MapPrinter.printAtlas("map", Visiwa::colorElevation);
    }
}
