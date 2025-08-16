package net.typho.lieutenant;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FillItem extends Item {
    public FillItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!user.hasPermissionLevel(2)) {
            return TypedActionResult.pass(stack);
        }

        if (user.isSneaking()) {
            stack.set(Lieutenant.FILL_ITEM_COMPONENT_TYPE, null);

            return TypedActionResult.success(stack);
        }

        HitResult hit = user.raycast(32, 1f, false);

        if (hit instanceof BlockHitResult blockHit) {
            BlockPos first = stack.get(Lieutenant.FILL_ITEM_COMPONENT_TYPE);
            BlockPos target = user.isSneaking() ? blockHit.getBlockPos().offset(blockHit.getSide()) : blockHit.getBlockPos();

            if (first == null) {
                stack.set(Lieutenant.FILL_ITEM_COMPONENT_TYPE, target);

                return TypedActionResult.success(stack);
            } else {
                stack.set(Lieutenant.FILL_ITEM_COMPONENT_TYPE, null);

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

                    for (BlockPos blockPos : BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ())) {
                        world.setBlockState(blockPos, state);
                    }

                    return TypedActionResult.success(stack);
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    public static BlockBox getSelection(PlayerEntity player, ItemStack wand, BlockHitResult hit) {
        BlockPos first = wand.get(Lieutenant.FILL_ITEM_COMPONENT_TYPE);
        BlockPos target = player.isSneaking() ? hit.getBlockPos().offset(hit.getSide()) : hit.getBlockPos();

        if (first == null) {
            return new BlockBox(target);
        }

        return BlockBox.create(first, target);
    }
}
