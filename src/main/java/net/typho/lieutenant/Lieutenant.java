package net.typho.lieutenant;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class Lieutenant implements ModInitializer {
    public static final String MOD_ID = "lieutenant";

    public static final ComponentType<BlockPos> FILL_ITEM_COMPONENT_TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "fill_item_component"), ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC).build());
    public static final Item FILL_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "fill"), new FillItem(new Item.Settings()));

    @Override
    public void onInitialize() {
    }
}
