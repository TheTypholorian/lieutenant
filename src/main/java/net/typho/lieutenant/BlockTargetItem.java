package net.typho.lieutenant;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockTargetItem {
    boolean setTarget(World world, BlockState block, BlockPos pos, PlayerEntity player, ItemStack stack);
}
