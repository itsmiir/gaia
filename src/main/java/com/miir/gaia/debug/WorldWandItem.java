package com.miir.gaia.debug;

import com.miir.gaia.gen.visiwa.Visiwa;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldWandItem extends Item {
    public WorldWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            BlockPos pos = user.getBlockPos();
            user.sendMessage(Text.literal(Visiwa.blockToAtlasCoord(pos.getX()) + ", " + Visiwa.blockToAtlasCoord(pos.getZ())), true);
//            Debug.generateWorldMap(((ServerWorld) world).getSeed(), user.isSneaking());
            return TypedActionResult.consume(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }
}
