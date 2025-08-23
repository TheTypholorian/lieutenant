package net.typho.lieutenant.client;

import com.mojang.brigadier.suggestion.Suggestion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.typho.lieutenant.Lieutenant;
import net.typho.lieutenant.SelectionItem;
import net.typho.lieutenant.SetFeatureS2CPacket;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class LieutenantClient implements ClientModInitializer {
    public static final KeyBinding SELECT_SELF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.lieutenant.select_self",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.lieutenant"
    ));

    public static boolean hasSuggestionIcon(Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());
        return id != null && Registries.ITEM.containsId(id);
    }

    public static void renderSuggestionIcon(DrawContext context, int x, int y, Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());

        if (id != null && Registries.ITEM.containsId(id)) {
            ItemStack stack = new ItemStack(Registries.ITEM.get(id));
            float scale = 2/3f;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.scale(scale, scale, 1);
            matrices.translate(x / scale + (28 * (1 - scale)), y / scale + (28 * (1 - scale)), 0);
            context.drawItem(stack, 0, 0);
            matrices.pop();
        }
    }

    public static Text keyTooltipText(KeyBinding key) {
        return Text.translatable("tooltip.lieutenant.key", Text.translatable(key.getBoundKeyTranslationKey()).getString()).formatted(Formatting.GOLD);
    }

    public static Text blockTooltipText(RegistryKey<Block> target) {
        return blockTooltipText(Registries.BLOCK.getOrEmpty(target).orElse(null));
    }

    public static Text blockTooltipText(Block block) {
        if (block == null) {
            return Text.translatable("tooltip.lieutenant.null_block");
        }

        return Text.translatable(block.getTranslationKey()).setStyle(Style.EMPTY.withColor(block.getDefaultMapColor() == MapColor.CLEAR ? -1 : block.getDefaultMapColor().color));
    }

    public static Text featureTooltipText(RegistryKey<ConfiguredFeature<?, ?>> target) {
        if (target == null) {
            return Text.translatable("tooltip.lieutenant.null_feature");
        }

        return Text.literal(target.getValue().toString());
    }

    public static Text fillTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.fill"
        );
    }

    public static Text cloneTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.clone",
                keyTooltipText(MinecraftClient.getInstance().options.useKey)
        );
    }

    public static Text selectTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.select",
                keyTooltipText(MinecraftClient.getInstance().options.useKey),
                keyTooltipText(MinecraftClient.getInstance().options.sneakKey)
        );
    }

    public static Text selectReplaceTooltipText() {
        return Text.translatable(
                "tooltip.lieutenant.select_replace",
                keyTooltipText(MinecraftClient.getInstance().options.pickItemKey)
        );
    }

    public static Text permissionTooltipText(int level) {
        return Text.translatable(
                "tooltip.lieutenant.permission",
                level
        );
    }

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hit) -> {
            PlayerEntity player = MinecraftClient.getInstance().player;

            if (player != null) {
                ItemStack held = player.getMainHandStack();

                if (held.getItem() instanceof SelectionItem selection && player.raycast(32, 1f, false) instanceof BlockHitResult blockHit) {
                    MatrixStack matrices = Objects.requireNonNull(context.matrixStack());
                    Vec3d cam = context.camera().getPos();

                    matrices.push();
                    matrices.translate(-cam.x, -cam.y, -cam.z);

                    selection.renderSelection(matrices, Objects.requireNonNull(context.consumers()).getBuffer(RenderLayer.getLines()), player, held, blockHit);

                    matrices.pop();

                    return false;
                }
            }

            return true;
        });
        ClientPlayNetworking.registerGlobalReceiver(SetFeatureS2CPacket.ID, (packet, context) -> Lieutenant.FEATURE_ITEM.feature = packet.feature());
        Lieutenant.CIRCLE_ITEM.radius = 3;
    }
}
