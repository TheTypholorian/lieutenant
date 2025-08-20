package net.typho.lieutenant;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class Lieutenant implements ModInitializer {
    public static final String MOD_ID = "lieutenant";

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
        PayloadTypeRegistry.playC2S().register(FeatureC2SPacket.ID, FeatureC2SPacket.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FillC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();

                if (packet.replace().isEmpty()) {
                    for (BlockPos blockPos : BlockPos.iterate(packet.box().getMinX(), packet.box().getMinY(), packet.box().getMinZ(), packet.box().getMaxX(), packet.box().getMaxY(), packet.box().getMaxZ())) {
                        world.setBlockState(blockPos, packet.fill());
                    }
                } else {
                    for (BlockPos blockPos : BlockPos.iterate(packet.box().getMinX(), packet.box().getMinY(), packet.box().getMinZ(), packet.box().getMaxX(), packet.box().getMaxY(), packet.box().getMaxZ())) {
                        if (world.getBlockState(blockPos).matchesKey(packet.replace().get())) {
                            world.setBlockState(blockPos, packet.fill());
                        }
                    }
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CircleC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();

                BlockBox box = new BlockBox(packet.pos()).expand(packet.radius());
                double radius = packet.radius() * packet.radius();

                if (packet.replace().isEmpty()) {
                    for (BlockPos blockPos : BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ())) {
                        if (blockPos.getSquaredDistance(packet.pos()) < radius) {
                            world.setBlockState(blockPos, packet.fill());
                        }
                    }
                } else {
                    for (BlockPos blockPos : BlockPos.iterate(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ())) {
                        if (blockPos.getSquaredDistance(packet.pos()) < radius && world.getBlockState(blockPos).matchesKey(packet.replace().get())) {
                            world.setBlockState(blockPos, packet.fill());
                        }
                    }
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(CloneC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();

                if (!CloneItem.execute((ServerWorld) world, packet.copy(), packet.paste())) {
                    context.player().sendMessage(Text.literal("Failed clone").formatted(Formatting.RED), true);
                }
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(FeatureC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                ServerWorld world = (ServerWorld) context.player().getWorld();
                ConfiguredFeature<?, ?> feature = world.getRegistryManager().get(RegistryKeys.CONFIGURED_FEATURE).get(packet.feature());

                if (feature == null) {
                    context.player().sendMessage(Text.literal("Nonexistent feature " + packet.feature()).formatted(Formatting.RED), true);
                } else if (!feature.generate(world, world.getChunkManager().getChunkGenerator(), world.random, packet.target())) {
                    context.player().sendMessage(Text.literal("Couldn't generate feature").formatted(Formatting.RED), true);
                }
            }
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("featureTool")
                    .executes(context -> {
                        PlayerEntity player = context.getSource().getPlayer();

                        if (player == null) {
                            return 0;
                        }

                        player.giveItemStack(new ItemStack(Lieutenant.FEATURE_ITEM));
                        return 1;
                    }));
        });
    }
}
