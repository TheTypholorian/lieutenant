package net.typho.lieutenant;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.typho.lieutenant.client.AlwaysDisplayNameItem;
import net.typho.lieutenant.client.LieutenantClient;

import java.util.List;
import java.util.Objects;

public class FillItem extends Item implements SelectionItem, BlockTargetItem, AlwaysDisplayNameItem {
    public FillItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean setTarget(World world, BlockState block, BlockPos pos, PlayerEntity player, ItemStack stack) {
        System.out.println("set target " + world.isClient);
        boolean clear = player.isSneaking();

        if (clear && stack.get(Lieutenant.BLOCK_TARGET_COMPONENT_TYPE) == null) {
            return false;
        }

        stack.set(Lieutenant.BLOCK_TARGET_COMPONENT_TYPE, clear ? null : Registries.BLOCK.getKey(block.getBlock()).orElseThrow());

        return true;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(
                getTranslationKey(stack),
                Objects.requireNonNull(MinecraftClient.getInstance().player).getOffHandStack().getItem() instanceof BlockItem block ? LieutenantClient.blockTooltipText(block.getBlock()) : Text.translatable("item.lieutenant.fill.off_hand"),
                Text.translatable("item.lieutenant.fill.replace", LieutenantClient.blockTooltipText(stack.get(Lieutenant.BLOCK_TARGET_COMPONENT_TYPE)))
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(LieutenantClient.fillTooltipText());
        tooltip.add(LieutenantClient.selectTooltipText());
        tooltip.add(LieutenantClient.fillReplacesTooltipText(stack.get(Lieutenant.BLOCK_TARGET_COMPONENT_TYPE)));
        tooltip.add(LieutenantClient.permissionTooltipText(2));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!user.hasPermissionLevel(2)) {
            return TypedActionResult.pass(stack);
        }

        HitResult hit = user.raycast(32, 1f, false);

        if (hit instanceof BlockHitResult blockHit) {
            BlockPos first = stack.get(Lieutenant.SINGLE_SELECTION_COMPONENT_TYPE);
            BlockPos target = user.isSneaking() ? blockHit.getBlockPos() : blockHit.getBlockPos().offset(blockHit.getSide());

            if (first == null) {
                stack.set(Lieutenant.SINGLE_SELECTION_COMPONENT_TYPE, target);

                return TypedActionResult.success(stack);
            } else {
                stack.set(Lieutenant.SINGLE_SELECTION_COMPONENT_TYPE, null);

                ItemPlacementContext placement = new ItemPlacementContext(world, user, hand, stack, blockHit);
                BlockState state;
                ItemStack offStack = user.getOffHandStack();

                if (!offStack.isEmpty() && offStack.getItem() instanceof BlockItem blockItem) {
                    state = blockItem.getBlock().getPlacementState(placement);
                } else {
                    state = Blocks.AIR.getPlacementState(placement);
                }

                BlockBox range = BlockBox.create(first, target);

                if (state != null) {
                    state = offStack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT).applyToState(state);
                    RegistryKey<Block> replace = stack.get(Lieutenant.BLOCK_TARGET_COMPONENT_TYPE);

                    if (replace == null) {
                        for (BlockPos blockPos : BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ())) {
                            world.setBlockState(blockPos, state);
                        }
                    } else {
                        for (BlockPos blockPos : BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ())) {
                            if (world.getBlockState(blockPos).matchesKey(replace)) {
                                world.setBlockState(blockPos, state);
                            }
                        }
                    }

                    return TypedActionResult.success(stack);
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void clearSelection(PlayerEntity player, World world, ItemStack stack) {
        stack.set(Lieutenant.SINGLE_SELECTION_COMPONENT_TYPE, null);
    }

    @Override
    public BlockBox getSelection(PlayerEntity player, ItemStack wand, BlockHitResult hit) {
        BlockPos first = wand.get(Lieutenant.SINGLE_SELECTION_COMPONENT_TYPE);
        BlockPos target = player.isSneaking() ? hit.getBlockPos() : hit.getBlockPos().offset(hit.getSide());

        if (first == null) {
            return new BlockBox(target);
        }

        return BlockBox.create(first, target);
    }
}
