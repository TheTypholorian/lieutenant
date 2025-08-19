package net.typho.lieutenant.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;

@Mixin(value = TeleportCommand.class, remap = false)
public abstract class TeleportCommandMixin {
    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    ordinal = 0
            )
    )
    private static ArgumentBuilder<ServerCommandSource, ?> argument(LiteralArgumentBuilder<ServerCommandSource> instance, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder, Operation<ArgumentBuilder<ServerCommandSource, ?>> original) {
        return original.call(instance, argumentBuilder)
                .then(
                        CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .executes(
                                        context -> {
                                            Vec3d pos = context.getSource().getEntityOrThrow().getPos();
                                            return TeleportCommand.execute(
                                                    context.getSource(),
                                                    Collections.singleton(context.getSource().getEntityOrThrow()),
                                                    DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                    DefaultPosArgument.absolute(pos.x, pos.y, pos.z),
                                                    DefaultPosArgument.zero(),
                                                    null
                                            );
                                        }
                                )
                                .then(
                                        CommandManager.argument("location", Vec3ArgumentType.vec3())
                                                .executes(
                                                        context -> TeleportCommand.execute(
                                                                context.getSource(),
                                                                Collections.singleton(context.getSource().getEntityOrThrow()),
                                                                DimensionArgumentType.getDimensionArgument(context, "dimension"),
                                                                Vec3ArgumentType.getPosArgument(context, "location"),
                                                                DefaultPosArgument.zero(),
                                                                null
                                                        )
                                                )
                                )
                );
    }
}
