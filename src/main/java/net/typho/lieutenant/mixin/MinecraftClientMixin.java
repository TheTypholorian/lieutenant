package net.typho.lieutenant.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.typho.lieutenant.BlockTargetItem;
import net.typho.lieutenant.SelectionItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V"
            )
    )
    private void clearSelection(CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack) {
        if (stack.getItem() instanceof SelectionItem selection && player != null) {
            selection.clearSelection(player, player.getWorld(), stack);
        }
    }

    @Inject(
            method = "doAttack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isAir()Z",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void setTarget(CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack, @Local BlockHitResult hit) {
        if (stack.getItem() instanceof BlockTargetItem target && player != null) {
            if (target.setTarget(player.getWorld(), player.getWorld().getBlockState(hit.getBlockPos()), hit.getBlockPos(), player, stack)) {
                cir.cancel();
            }
        }
    }
}
