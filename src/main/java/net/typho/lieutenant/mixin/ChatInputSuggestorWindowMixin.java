package net.typho.lieutenant.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.util.math.Rect2i;
import net.typho.lieutenant.client.LieutenantClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public class ChatInputSuggestorWindowMixin {
    @Shadow
    @Final
    private Rect2i area;

    @Shadow
    @Final
    private List<Suggestion> suggestions;

    @Shadow
    private int inWindowIndex;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
            )
    )
    private void render(DrawContext context, int mouseX, int mouseY, CallbackInfo ci, @Local(name = "l") int l) {
        LieutenantClient.renderSuggestionIcon(context, area.getX() - 4, area.getY() - 5 + 12 * l, mouseX, mouseY, suggestions.get(l + inWindowIndex));
    }

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/Rect2i;getX()I",
                    ordinal = 12
            )
    )
    private int offsetX(Rect2i instance, Operation<Integer> original, @Local Suggestion suggestion) {
        if (LieutenantClient.hasSuggestionIcon(suggestion)) {
            return original.call(instance) + 14;
        }

        return original.call(instance);
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/Rect2i;<init>(IIII)V"
            ),
            index = 2
    )
    private int offsetWidth(int w, @Local(argsOnly = true) List<Suggestion> suggestions) {
        if (suggestions.stream().anyMatch(LieutenantClient::hasSuggestionIcon)) {
            return w + 14;
        }

        return w;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/Rect2i;<init>(IIII)V"
            ),
            index = 0
    )
    private int offsetX(int w, @Local(argsOnly = true) List<Suggestion> suggestions) {
        if (suggestions.stream().anyMatch(LieutenantClient::hasSuggestionIcon)) {
            return w - 14;
        }

        return w;
    }
}
