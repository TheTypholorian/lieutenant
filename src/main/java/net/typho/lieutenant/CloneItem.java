package net.typho.lieutenant;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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
import net.typho.lieutenant.client.AlwaysDisplayNameItem;
import net.typho.lieutenant.client.LieutenantClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CloneItem extends Item implements DualSelectionItem, TargetedItem, AltScrollItem, AlwaysDisplayNameItem {
    @Environment(EnvType.CLIENT)
    public BlockBox selection;
    @Environment(EnvType.CLIENT)
    public CloneType type;

    public CloneItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(
                getTranslationKey(stack),
                Text.translatable(type.getTranslationKey())
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(LieutenantClient.cloneTooltipText());
        tooltip.add(LieutenantClient.selectTooltipText());
        tooltip.add(LieutenantClient.permissionTooltipText(2));
    }

    @Override
    @SuppressWarnings("deprecation")
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            if (!user.hasPermissionLevel(2)) {
                return TypedActionResult.pass(stack);
            }

            HitResult hit = user.raycast(32, 1f, false);

            if (hit instanceof BlockHitResult blockHit) {
                BlockPos target = getTarget(user, blockHit);

                if (selection == null) {
                    selection = new BlockBox(target);
                    return TypedActionResult.success(stack);
                } else if (selection.getMinX() == selection.getMaxX() && selection.getMinY() == selection.getMaxY() && selection.getMinZ() == selection.getMaxZ()) {
                    selection.encompass(target);
                    return TypedActionResult.success(stack);
                } else {
                    BlockBox targetBox = selection.offset(target.getX() - selection.getMinX(), target.getY() - selection.getMinY(), target.getZ() - selection.getMinZ());

                    if (selection.intersects(targetBox)) {
                        user.sendMessage(Text.translatable("error.lieutenant.clone_overlap").formatted(Formatting.RED), true);
                        return TypedActionResult.fail(stack);
                    }

                    ClientPlayNetworking.send(new CloneC2SPacket(selection, target, type));

                    return TypedActionResult.success(stack);
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void clearSelection(PlayerEntity player, World world, ItemStack stack) {
        if (world.isClient) {
            selection = null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockBox getSelection(PlayerEntity player, ItemStack wand, BlockHitResult hit) {
        BlockPos target = getTarget(player, hit);

        if (selection == null) {
            return new BlockBox(target);
        } else if (selection.getMinX() == selection.getMaxX() && selection.getMinY() == selection.getMaxY() && selection.getMinZ() == selection.getMaxZ()) {
            return new BlockBox(selection.getMinX(), selection.getMinY(), selection.getMinZ(), selection.getMaxX(), selection.getMaxY(), selection.getMaxZ()).encompass(target);
        }

        return selection;
    }

    @Override
    public BlockBox getOtherSelection(PlayerEntity player, ItemStack stack, BlockHitResult blockHit) {
        BlockPos target = getTarget(player, blockHit);

        if (selection == null || (selection.getMinX() == selection.getMaxX() && selection.getMinY() == selection.getMaxY() && selection.getMinZ() == selection.getMaxZ())) {
            return null;
        }

        return selection.offset(target.getX() - selection.getMinX(), target.getY() - selection.getMinY(), target.getZ() - selection.getMinZ());
    }

    @Override
    public void scroll(PlayerEntity player, ItemStack stack, double amount) {
        type = Math.signum(amount) == 1 ? type.prev() : type.next();
    }

    @SuppressWarnings("deprecation")
    public static boolean execute(ServerWorld serverWorld, BlockBox sourceBox, BlockPos destination, CloneType type) {
        record BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
        }

        record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo) {
        }

        BlockPos blockPos4 = destination.add(sourceBox.getDimensions());
        BlockBox blockBox2 = BlockBox.create(destination, blockPos4);

        if (serverWorld.isRegionLoaded(sourceBox.getMinX(), sourceBox.getMinY(), sourceBox.getMinZ(), sourceBox.getMaxX(), sourceBox.getMaxY(), sourceBox.getMaxZ())) {
            List<BlockInfo> list = Lists.newLinkedList();
            List<BlockInfo> list2 = Lists.newLinkedList();
            List<BlockInfo> list3 = Lists.newLinkedList();
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

                        if (type.test(blockState)) {
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
            }

            List<BlockInfo> list4 = Lists.newLinkedList();
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
