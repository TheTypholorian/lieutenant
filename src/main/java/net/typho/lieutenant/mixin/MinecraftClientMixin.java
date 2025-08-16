package net.typho.lieutenant.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.typho.lieutenant.CustomPickItem;
import net.typho.lieutenant.SelectionItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Nullable
    public ClientWorld world;

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
            method = "doItemPick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doItemPickAir(CallbackInfo ci) {
        assert player != null;
        ItemStack held = player.getMainHandStack();

        if (held.getItem() instanceof CustomPickItem pick && crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.MISS && pick.pick(held, ItemStack.EMPTY, Blocks.AIR.getDefaultState(), player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doItemPick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"
            ),
            cancellable = true
    )
    private void doItemPick(CallbackInfo ci, @Local ItemStack stack) {
        assert player != null;
        ItemStack held = player.getMainHandStack();

        if (held.getItem() instanceof CustomPickItem pick && crosshairTarget != null && world != null && crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) this.crosshairTarget).getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (pick.pick(held, stack, blockState, player)) {
                ci.cancel();
            }
        }
    }
}
