package net.typho.lieutenant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.typho.lieutenant.client.LieutenantClient;

public interface TargetedItem {
    default BlockPos getTarget(PlayerEntity user, BlockHitResult hit) {
        if (LieutenantClient.SELECT_SELF.isPressed()) {
            return user.getBlockPos();
        }

        if (user.isSneaking()) {
            return hit.getBlockPos();
        }

        return hit.getBlockPos().offset(hit.getSide());
    }
}
