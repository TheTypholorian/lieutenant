package net.typho.lieutenant.client;

import net.minecraft.item.ItemStack;

public interface AlwaysDisplayNameItem {
    default boolean shouldDisplay(ItemStack stack) {
        return true;
    }
}
