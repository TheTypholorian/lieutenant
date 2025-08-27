package net.typho.lieutenant.mixin.tools;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.PlaceCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.typho.lieutenant.Lieutenant;
import net.typho.lieutenant.packets.SetFeatureS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PlaceCommand.class)
public class PlaceCommandMixin {
    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;",
                    ordinal = 1
            )
    )
    private static LiteralArgumentBuilder<ServerCommandSource> register(String literal, Operation<LiteralArgumentBuilder<ServerCommandSource>> original) {
        return original.call(literal).executes(context -> {
            if (context.getSource().getPlayer() != null) {
                context.getSource().getPlayer().giveItemStack(new ItemStack(Lieutenant.FEATURE_ITEM));
                return 1;
            }

            return 0;
        });
    }

    @Inject(
            method = "method_39990",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void executeFeature(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        if (context.getSource().isExecutedByPlayer()) {
            ServerPlayerEntity player = Objects.requireNonNull(context.getSource().getPlayer());

            if (player.getMainHandStack().getItem() == Lieutenant.FEATURE_ITEM) {
                ServerPlayNetworking.send(player, new SetFeatureS2CPacket(RegistryKeyArgumentType.getConfiguredFeatureEntry(context, "feature").registryKey()));
                cir.cancel();
            }
        }
    }
}
