package net.typho.lieutenant;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface CustomPickItem {
    boolean pick(ItemStack held, ItemStack target, BlockState targetBlock, PlayerEntity player);
}
