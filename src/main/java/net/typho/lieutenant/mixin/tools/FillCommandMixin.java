package net.typho.lieutenant.mixin.tools;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.FillCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.typho.lieutenant.Lieutenant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = FillCommand.class, remap = false)
public class FillCommandMixin {
    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    ordinal = 0
            )
    )
    private static ArgumentBuilder<?, ?> register(LiteralArgumentBuilder<ServerCommandSource> instance, Predicate<ServerCommandSource> predicate, Operation<ArgumentBuilder<ServerCommandSource, ?>> original) {
        return original.call(instance, predicate).executes(context -> {
            if (context.getSource().getPlayer() != null) {
                context.getSource().getPlayer().giveItemStack(new ItemStack(Lieutenant.FILL_ITEM));
                return 1;
            }

            return 0;
        });
    }
}
