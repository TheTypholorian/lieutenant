package net.typho.lieutenant.mixin.qol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(value = EnchantCommand.class, remap = false)
public class EnchantCommandMixin {
    @Shadow
    @Final
    private static DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION;

    @Shadow
    @Final
    private static DynamicCommandExceptionType FAILED_ENTITY_EXCEPTION;

    @Shadow
    @Final
    private static SimpleCommandExceptionType FAILED_EXCEPTION;

    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;executes(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    ordinal = 1
            )
    )
    private static ArgumentBuilder<ServerCommandSource, ?> argument(RequiredArgumentBuilder<ServerCommandSource, ?> instance, Command<ServerCommandSource> command, Operation<ArgumentBuilder<ServerCommandSource, ?>> original) {
        return original.call(instance, command)
                .then(CommandManager.literal("force")
                        .executes(context -> {
                            Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
                            RegistryEntry<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
                            int level = IntegerArgumentType.getInteger(context, "level");
                            int i = 0;

                            for (Entity entity : targets) {
                                if (entity instanceof LivingEntity livingEntity) {
                                    ItemStack itemStack = livingEntity.getMainHandStack();

                                    if (!itemStack.isEmpty()) {
                                        itemStack.addEnchantment(enchantment, level);
                                        i++;
                                    } else if (targets.size() == 1) {
                                        throw FAILED_ITEMLESS_EXCEPTION.create(livingEntity.getName().getString());
                                    }
                                } else if (targets.size() == 1) {
                                    throw FAILED_ENTITY_EXCEPTION.create(entity.getName().getString());
                                }
                            }

                            if (i == 0) {
                                throw FAILED_EXCEPTION.create();
                            } else {
                                if (targets.size() == 1) {
                                    context.getSource().sendFeedback(
                                            () -> Text.translatable("commands.enchant.success.single", Enchantment.getName(enchantment, level), ((Entity) targets.iterator().next()).getDisplayName()),
                                            true
                                    );
                                } else {
                                    context.getSource().sendFeedback(() -> Text.translatable("commands.enchant.success.multiple", Enchantment.getName(enchantment, level), targets.size()), true);
                                }

                                return i;
                            }
                        }));
    }
}
