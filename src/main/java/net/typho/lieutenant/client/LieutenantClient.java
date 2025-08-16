package net.typho.lieutenant.client;

import com.mojang.brigadier.suggestion.Suggestion;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.typho.lieutenant.SelectionItem;

import java.util.Objects;

public class LieutenantClient implements ClientModInitializer {
    public static boolean hasSuggestionIcon(Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());
        return id != null && Registries.ITEM.containsId(id);
    }

    public static void renderSuggestionIcon(DrawContext context, int x, int y, int mouseX, int mouseY, Suggestion suggestion) {
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
    }
}
