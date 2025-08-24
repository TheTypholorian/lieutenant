package net.typho.lieutenant;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.typho.lieutenant.client.AlwaysDisplayNameItem;
import net.typho.lieutenant.client.LieutenantClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CircleItem extends Item implements SelectionItem, AlwaysDisplayNameItem, CustomPickItem, TargetedItem, AltScrollItem {
    @Environment(EnvType.CLIENT)
    public RegistryKey<Block> replace;
    @Environment(EnvType.CLIENT)
    public int radius;

    public CircleItem(Settings settings) {
        super(settings);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(
                getTranslationKey(stack),
                LieutenantClient.blockTooltipText(Objects.requireNonNull(MinecraftClient.getInstance().player).getOffHandStack().getItem() instanceof BlockItem block ? block.getBlock() : Blocks.AIR),
                radius,
                LieutenantClient.blockTooltipText(replace)
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(LieutenantClient.circleTooltipText());
        tooltip.add(LieutenantClient.radiusTooltipText());
        tooltip.add(LieutenantClient.selectTooltipText());
        tooltip.add(LieutenantClient.selectReplaceTooltipText());
        tooltip.add(LieutenantClient.permissionTooltipText(2));
    }

    @Override
    public void scroll(PlayerEntity player, ItemStack stack, double amount) {
        radius = MathHelper.clamp(radius + (int) Math.signum(amount), 0, 256);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            if (!user.hasPermissionLevel(2)) {
                return TypedActionResult.pass(stack);
            }

            HitResult hit = user.raycast(32, 1f, false);

            if (hit instanceof BlockHitResult blockHit) {
                BlockPos target = getTarget(user, blockHit);
                ItemStack offStack = user.getOffHandStack();
                RegistryKey<Block> fill = Registries.BLOCK.getKey(!offStack.isEmpty() && offStack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock() : Blocks.AIR).orElseThrow();

                ClientPlayNetworking.send(new CircleC2SPacket(target, fill, radius, Optional.ofNullable(replace)));

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public boolean pick(ItemStack held, ItemStack target, BlockState targetBlock, PlayerEntity player) {
        replace = targetBlock.isAir() && !player.isSneaking() ? null : Registries.BLOCK.getKey(targetBlock.getBlock()).orElseThrow();
        return true;
    }

    @Override
    public void clearSelection(PlayerEntity player, World world, ItemStack stack) {
    }

    @Override
    public BlockBox getSelection(PlayerEntity player, ItemStack wand, BlockHitResult hit) {
        return new BlockBox(getTarget(player, hit)).expand(radius);
    }

    @Override
    public boolean canSelectSelf() {
        return false;
    }
}
