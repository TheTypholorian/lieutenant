package net.typho.lieutenant.mixin.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.typho.lieutenant.client.LieutenantClient;
import net.typho.lieutenant.input.AltScrollItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerInventoryMixin {
    @Shadow
    public abstract ItemStack getMainHandStack();

    @Shadow
    @Final
    public PlayerEntity player;

    @Inject(
            method = "scrollInHotbar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scrollInHotbar(double scrollAmount, CallbackInfo ci) {
        if (LieutenantClient.SELECT_SELF.isPressed() && getMainHandStack().getItem() instanceof AltScrollItem alt) {
            alt.scroll(player, getMainHandStack(), scrollAmount);
            ci.cancel();
        }
    }
}
