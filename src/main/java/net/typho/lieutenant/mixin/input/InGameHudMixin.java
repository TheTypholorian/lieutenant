package net.typho.lieutenant.mixin.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.typho.lieutenant.input.AlwaysDisplayNameItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    private ItemStack currentStack;

    @WrapOperation(
            method = "renderHeldItemTooltip",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;heldItemTooltipFade:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int renderHeldItemTooltip(InGameHud instance, Operation<Integer> original) {
        if (currentStack.getItem() instanceof AlwaysDisplayNameItem item && item.shouldDisplay(currentStack)) {
            return 40;
        }

        return original.call(instance);
    }
}
