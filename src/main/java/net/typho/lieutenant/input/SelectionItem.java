package net.typho.lieutenant.input;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.World;

public interface SelectionItem {
    default void renderSelection(MatrixStack matrices, VertexConsumer consumer, PlayerEntity player, ItemStack stack, BlockHitResult blockHit) {
        BlockBox box = getSelection(player, stack, blockHit);

        if (box != null) {
            WorldRenderer.drawBox(
                    matrices,
                    consumer,
                    box.getMinX(), box.getMinY(), box.getMinZ(),
                    box.getMaxX() + 1, box.getMaxY() + 1, box.getMaxZ() + 1,
                    1, 1, 1, 1,
                    0.5f, 0.5f, 0.5f
            );
        }
    }

    void clearSelection(PlayerEntity player, World world, ItemStack stack);

    BlockBox getSelection(PlayerEntity player, ItemStack stack, BlockHitResult blockHit);
}
