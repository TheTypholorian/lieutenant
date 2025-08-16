package net.typho.lieutenant;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class Lieutenant implements ModInitializer {
    public static final String MOD_ID = "lieutenant";

    public static final KeyBinding SELECT_SELF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lieutenant.select_self",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.lieutenant"
    ));

    public static final Item FILL_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "fill"), new FillItem(new Item.Settings().maxCount(1)));
    public static final Item CLONE_ITEM = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "clone"), new CloneItem(new Item.Settings().maxCount(1)));

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
        PayloadTypeRegistry.playC2S().register(CloneC2SPacket.ID, CloneC2SPacket.PACKET_CODEC);
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
        ServerPlayNetworking.registerGlobalReceiver(CloneC2SPacket.ID, (packet, context) -> {
            if (context.player().hasPermissionLevel(2)) {
                World world = context.player().getWorld();

                if (!CloneItem.execute((ServerWorld) world, packet.copy(), packet.paste())) {
                    context.player().sendMessage(Text.literal("Failed clone").formatted(Formatting.RED), true);
                }
            }
        });
    }
}
