package net.typho.lieutenant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class Lieutenant implements ModInitializer {
    public static final String MOD_ID = "lieutenant";

    public static final ComponentType<BlockPos> SINGLE_SELECTION_COMPONENT_TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "single_selection"), ComponentType.<BlockPos>builder().codec(BlockPos.CODEC).packetCodec(BlockPos.PACKET_CODEC).build());
    public static final ComponentType<BlockBox> BOX_SELECTION_COMPONENT_TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "box_selection"), ComponentType.<BlockBox>builder().codec(BlockBox.CODEC).build());
    public static final ComponentType<RegistryKey<Block>> BLOCK_TARGET_COMPONENT_TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "block_target"), ComponentType.<RegistryKey<Block>>builder().codec(RegistryKey.createCodec(RegistryKeys.BLOCK)).build());

    public static final Item FILL_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "fill"), new FillItem(new Item.Settings()));
    public static final Item CLONE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "clone"), new CloneItem(new Item.Settings()));

    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register((user, world, hand, blockPos, direction) -> {
            ItemStack stack = user.getStackInHand(hand);

            if (stack.getItem() instanceof SelectionItem selection) {
                selection.clearSelection(user, world, stack);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }
}
