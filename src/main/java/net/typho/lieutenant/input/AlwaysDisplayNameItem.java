package net.typho.lieutenant.input;

import net.minecraft.item.ItemStack;

public interface AlwaysDisplayNameItem {
    default boolean shouldDisplay(ItemStack stack) {
        return true;
    }
}
