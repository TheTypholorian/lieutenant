package net.typho.lieutenant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.typho.lieutenant.input.SelectionItem;
import net.typho.lieutenant.packets.*;
import net.typho.lieutenant.tools.CircleItem;
import net.typho.lieutenant.tools.CloneItem;
import net.typho.lieutenant.tools.FeatureItem;
import net.typho.lieutenant.tools.FillItem;

import java.util.Optional;

public class Lieutenant implements ModInitializer {
    public static final String MOD_ID = "lieutenant";

    public static final PacketCodec<PacketByteBuf, BlockBox> BLOCK_BOX_PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, BlockBox::getMinX,
            PacketCodecs.INTEGER, BlockBox::getMinY,
            PacketCodecs.INTEGER, BlockBox::getMinZ,
            PacketCodecs.INTEGER, BlockBox::getMaxX,
            PacketCodecs.INTEGER, BlockBox::getMaxY,
            PacketCodecs.INTEGER, BlockBox::getMaxZ,
            BlockBox::new
    );

    public static final FillItem FILL_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "fill"), new FillItem(new Item.Settings().maxCount(1)));
    public static final CloneItem CLONE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "clone"), new CloneItem(new Item.Settings().maxCount(1)));
    public static final CircleItem CIRCLE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "circle"), new CircleItem(new Item.Settings().maxCount(1)));
    public static final FeatureItem FEATURE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "feature"), new FeatureItem(new Item.Settings().maxCount(1)));

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
        PayloadTypeRegistry.playC2S().register(FillC2SPacket.ID, FillC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CircleC2SPacket.ID, CircleC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(CloneC2SPacket.ID, CloneC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(PlaceFeatureC2SPacket.ID, PlaceFeatureC2SPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SetFeatureS2CPacket.ID, SetFeatureS2CPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FillC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();
                Optional<Block> fill = Registries.BLOCK.getOrEmpty(packet.fill());

                if (fill.isEmpty()) {
                    context.player().sendMessage(Text.translatable("error.lieutenant.nonexistent_block", packet.fill()).formatted(Formatting.RED), true);
                } else {
                    if (context.player().raycast(32, 1f, false) instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
                        BlockState state = fill.get().getPlacementState(new ItemPlacementContext(new ItemUsageContext(context.player(), Hand.MAIN_HAND, blockHit)));

                        if (packet.replace().isEmpty()) {
                            for (BlockPos blockPos : BlockPos.iterate(packet.box().getMinX(), packet.box().getMinY(), packet.box().getMinZ(), packet.box().getMaxX(), packet.box().getMaxY(), packet.box().getMaxZ())) {
                                world.setBlockState(blockPos, state);
                            }
                        } else {
                            for (BlockPos blockPos : BlockPos.iterate(packet.box().getMinX(), packet.box().getMinY(), packet.box().getMinZ(), packet.box().getMaxX(), packet.box().getMaxY(), packet.box().getMaxZ())) {
                                if (world.getBlockState(blockPos).matchesKey(packet.replace().get())) {
                                    world.setBlockState(blockPos, state);
                                }
                            }
                        }
                    }
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CircleC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();
                Optional<Block> fill = Registries.BLOCK.getOrEmpty(packet.fill());

                if (fill.isEmpty()) {
                    context.player().sendMessage(Text.translatable("error.lieutenant.nonexistent_block", packet.fill()).formatted(Formatting.RED), true);
                } else {
                    BlockState state = fill.get().getDefaultState();

                    BlockBox box = new BlockBox(packet.pos()).expand(packet.radius());
                    double radius = packet.radius() * packet.radius();

                    if (packet.replace().isEmpty()) {
                        for (BlockPos blockPos : BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ())) {
                            if (blockPos.getSquaredDistance(packet.pos()) < radius) {
                                world.setBlockState(blockPos, state);
                            }
                        }
                    } else {
                        for (BlockPos blockPos : BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ())) {
                            if (blockPos.getSquaredDistance(packet.pos()) < radius && world.getBlockState(blockPos).matchesKey(packet.replace().get())) {
                                world.setBlockState(blockPos, state);
                            }
                        }
                    }
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CloneC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();

                if (!CloneItem.execute((ServerWorld) world, packet.copy(), packet.paste(), packet.type())) {
                    context.player().sendMessage(Text.translatable("error.lieutenant.failed_clone").formatted(Formatting.RED), true);
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(PlaceFeatureC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                ServerWorld world = (ServerWorld) context.player().getWorld();
                ConfiguredFeature<?, ?> feature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(packet.feature());

                if (feature == null) {
                    context.player().sendMessage(Text.translatable("error.lieutenant.feature_doesnt_exist" + packet.feature()).formatted(Formatting.RED), true);
                } else if (!feature.generate(world, world.getChunkManager().getChunkGenerator(), world.random, packet.target())) {
                    context.player().sendMessage(Text.translatable("error.lieutenant.generate_feature").formatted(Formatting.RED), true);
                }
            }
        });
    }
}
