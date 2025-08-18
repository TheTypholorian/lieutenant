package net.typho.lieutenant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface AltScrollItem {
    void scroll(PlayerEntity player, ItemStack stack, double amount);
}
