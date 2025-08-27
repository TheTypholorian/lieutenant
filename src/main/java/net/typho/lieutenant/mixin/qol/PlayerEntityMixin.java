package net.typho.lieutenant.mixin.qol;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PlayerEntity.class, priority = 900)
public class PlayerEntityMixin {
    @ModifyConstant(
            method = "getOffGroundSpeed",
            constant = @Constant(floatValue = 2.0F)
    )
    private float sprintFlyingSpeed(float constant) {
        return 5f;
    }
}
