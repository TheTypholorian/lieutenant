package net.typho.lieutenant;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CloneItem extends Item implements DualSelectionItem {
    public CloneItem(Settings settings) {
        super(settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!user.hasPermissionLevel(2)) {
            return TypedActionResult.pass(stack);
        }

        HitResult hit = user.raycast(32, 1f, false);

        if (hit instanceof BlockHitResult blockHit) {
            BlockBox box = stack.get(Lieutenant.BOX_SELECTION_COMPONENT_TYPE);
            BlockPos target = user.isSneaking() ? blockHit.getBlockPos().offset(blockHit.getSide()) : blockHit.getBlockPos();

            if (box == null) {
                stack.set(Lieutenant.BOX_SELECTION_COMPONENT_TYPE, new BlockBox(target));
                return TypedActionResult.success(stack);
            } else if (box.getMinX() == box.getMaxX() && box.getMinY() == box.getMaxY() && box.getMinZ() == box.getMaxZ()) {
                stack.set(Lieutenant.BOX_SELECTION_COMPONENT_TYPE, box.encompass(target));
                return TypedActionResult.success(stack);
            } else {
                BlockBox targetBox = box.offset(target.getX() - box.getMinX(), target.getY() - box.getMinY(), target.getZ() - box.getMinZ());

                if (box.intersects(targetBox)) {
                    user.sendMessage(Text.literal("Source and destination areas overlap").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(stack);
                }

                if (world instanceof ServerWorld server) {
                    if (!execute(server, box, target)) {
                        user.sendMessage(Text.literal("Failed clone").formatted(Formatting.RED), true);
                        return TypedActionResult.fail(stack);
                    }
                }

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void clearSelection(PlayerEntity player, World world, ItemStack stack) {
        stack.set(Lieutenant.BOX_SELECTION_COMPONENT_TYPE, null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockBox getSelection(PlayerEntity player, ItemStack wand, BlockHitResult hit) {
        BlockBox box = wand.get(Lieutenant.BOX_SELECTION_COMPONENT_TYPE);
        BlockPos target = player.isSneaking() ? hit.getBlockPos().offset(hit.getSide()) : hit.getBlockPos();

        if (box == null) {
            return new BlockBox(target);
        } else if (box.getMinX() == box.getMaxX() && box.getMinY() == box.getMaxY() && box.getMinZ() == box.getMaxZ()) {
            return new BlockBox(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ()).encompass(target);
        }

        return box;
    }

    @Override
    public BlockBox getOtherSelection(PlayerEntity player, ItemStack stack, BlockHitResult blockHit) {
        BlockBox box = stack.get(Lieutenant.BOX_SELECTION_COMPONENT_TYPE);
        BlockPos target = player.isSneaking() ? blockHit.getBlockPos().offset(blockHit.getSide()) : blockHit.getBlockPos();

        if (box == null || (box.getMinX() == box.getMaxX() && box.getMinY() == box.getMaxY() && box.getMinZ() == box.getMaxZ())) {
            return null;
        }

        return box.offset(target.getX() - box.getMinX(), target.getY() - box.getMinY(), target.getZ() - box.getMinZ());
    }

    @SuppressWarnings("deprecation")
    public static boolean execute(ServerWorld serverWorld, BlockBox sourceBox, BlockPos destination) {
        record BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
        }

        record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo) {
        }

        BlockPos blockPos4 = destination.add(sourceBox.getDimensions());
        BlockBox blockBox2 = BlockBox.create(destination, blockPos4);

        if (serverWorld.isRegionLoaded(sourceBox.getMinX(), sourceBox.getMinY(), sourceBox.getMinZ(), sourceBox.getMaxX(), sourceBox.getMaxY(), sourceBox.getMaxZ())) {
            List<BlockInfo> list = Lists.newArrayList();
            List<BlockInfo> list2 = Lists.newArrayList();
            List<BlockInfo> list3 = Lists.newArrayList();
            BlockPos blockPos5 = new BlockPos(
                    blockBox2.getMinX() - sourceBox.getMinX(), blockBox2.getMinY() - sourceBox.getMinY(), blockBox2.getMinZ() - sourceBox.getMinZ()
            );

            for (int k = sourceBox.getMinZ(); k <= sourceBox.getMaxZ(); k++) {
                for (int l = sourceBox.getMinY(); l <= sourceBox.getMaxY(); l++) {
                    for (int m = sourceBox.getMinX(); m <= sourceBox.getMaxX(); m++) {
                        BlockPos blockPos6 = new BlockPos(m, l, k);
                        BlockPos blockPos7 = blockPos6.add(blockPos5);
                        CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(serverWorld, blockPos6, false);
                        BlockState blockState = cachedBlockPosition.getBlockState();

                        BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos6);
                        if (blockEntity != null) {
                            BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                    blockEntity.createComponentlessNbt(serverWorld.getRegistryManager()), blockEntity.getComponents()
                            );
                            list2.add(new BlockInfo(blockPos7, blockState, blockEntityInfo));
                        } else if (!blockState.isOpaqueFullCube(serverWorld, blockPos6) && !blockState.isFullCube(serverWorld, blockPos6)) {
                            list3.add(new BlockInfo(blockPos7, blockState, null));
                        } else {
                            list.add(new BlockInfo(blockPos7, blockState, null));
                        }
                    }
                }
            }

            List<BlockInfo> list4 = Lists.newArrayList();
            list4.addAll(list);
            list4.addAll(list2);
            list4.addAll(list3);
            List<BlockInfo> list5 = Lists.reverse(list4);

            for (BlockInfo blockInfo : list5) {
                BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos);
                Clearable.clear(blockEntity3);
                serverWorld.setBlockState(blockInfo.pos, Blocks.BARRIER.getDefaultState(), Block.NOTIFY_LISTENERS);
            }

            int mx = 0;

            for (BlockInfo blockInfo2 : list4) {
                if (serverWorld.setBlockState(blockInfo2.pos, blockInfo2.state, Block.NOTIFY_LISTENERS)) {
                    mx++;
                }
            }

            for (BlockInfo blockInfo2x : list2) {
                BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo2x.pos);
                if (blockInfo2x.blockEntityInfo != null && blockEntity4 != null) {
                    blockEntity4.readComponentlessNbt(blockInfo2x.blockEntityInfo.nbt, serverWorld.getRegistryManager());
                    blockEntity4.setComponents(blockInfo2x.blockEntityInfo.components);
                    blockEntity4.markDirty();
                }

                serverWorld.setBlockState(blockInfo2x.pos, blockInfo2x.state, Block.NOTIFY_LISTENERS);
            }

            for (BlockInfo blockInfo2x : list5) {
                serverWorld.updateNeighbors(blockInfo2x.pos, blockInfo2x.state.getBlock());
            }

            serverWorld.getBlockTickScheduler().scheduleTicks(serverWorld.getBlockTickScheduler(), sourceBox, blockPos5);

            if (mx == 0) {
                return false;
            } else {
                return mx > 0;
            }
        } else {
            return false;
        }
    }
}
