package net.typho.lieutenant;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;

public interface DualSelectionItem extends SelectionItem {
    @Override
    default void renderSelection(MatrixStack matrices, VertexConsumer consumer, PlayerEntity player, ItemStack stack, BlockHitResult blockHit) {
        SelectionItem.super.renderSelection(matrices, consumer, player, stack, blockHit);

        BlockBox box = getOtherSelection(player, stack, blockHit);

        if (box != null) {
            WorldRenderer.drawBox(
                    matrices,
                    consumer,
                    box.getMinX(), box.getMinY(), box.getMinZ(),
                    box.getMaxX() + 1, box.getMaxY() + 1, box.getMaxZ() + 1,
                    0.75f, 0.75f, 0.75f, 0.75f,
                    0.5f, 0.5f, 0.5f
            );
        }
    }

    BlockBox getOtherSelection(PlayerEntity player, ItemStack stack, BlockHitResult blockHit);
}
