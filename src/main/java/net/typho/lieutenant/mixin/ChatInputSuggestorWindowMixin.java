package net.typho.lieutenant.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.util.math.Rect2i;
import net.typho.lieutenant.client.LieutenantClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public abstract class ChatInputSuggestorWindowMixin {
    @Shadow
    @Final
    private Rect2i area;

    @Shadow
    @Final
    private List<Suggestion> suggestions;

    @Shadow
    private int inWindowIndex;

    @Shadow
    public abstract void select(int index);

    @Unique
    private boolean hasIcons = false;
    @Unique
    private boolean hasScroll = false;

    @Unique
    private ChatInputSuggestor parent;

    @Inject(
            method = "<init>",
            at = @At("CTOR_HEAD")
    )
    private void init(ChatInputSuggestor parent, int x, int y, int width, List<Suggestion> suggestions, boolean narrateFirstSuggestion, CallbackInfo ci) {
        this.parent = parent;
        hasIcons = suggestions.stream().anyMatch(LieutenantClient::hasSuggestionIcon);
        hasScroll = suggestions.size() > parent.maxSuggestionSize;
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
            )
    )
    private void renderIcons(DrawContext context, int mouseX, int mouseY, CallbackInfo ci, @Local(name = "l") int l) {
        LieutenantClient.renderSuggestionIcon(context, area.getX() - 4, area.getY() - 5 + 12 * l, suggestions.get(l + inWindowIndex));
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderScroll(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (hasScroll) {
            LieutenantClient.renderScrollBar(context, area.getX(), area.getY(), 2, area.getHeight(), inWindowIndex, suggestions.size() - parent.maxSuggestionSize);
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor$SuggestionWindow;select(I)V"
            ),
            cancellable = true
    )
    private void mouseClicked(int x, int y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (hasScroll && x - area.getX() < 2) {
            inWindowIndex = (int) ((float) (y - area.getY()) / area.getHeight() * (suggestions.size() - parent.maxSuggestionSize));
            cir.cancel();
        }
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
        int x = original.call(instance);

        if (hasIcons) {
            x += 14;
        }

        if (hasScroll) {
            x += 2;
        }

        return x;
    }

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/Rect2i;<init>(IIII)V"
            ),
            index = 2
    )
    private int offsetWidth(int w) {
        if (hasIcons) {
            w += 14;
        }

        if (hasScroll) {
            w += 2;
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
        if (hasIcons) {
            w -= 14;
        }

        if (hasScroll) {
            w -= 2;
        }

        return w;
    }

    @ModifyVariable(
            method = "mouseScrolled",
            at = @At("HEAD"),
            argsOnly = true
    )
    private double amount(double value) {
        if (MinecraftClient.getInstance().options.sprintKey.isPressed()) {
            return value * 3;
        }

        return value;
    }

    @Inject(
            method = "mouseScrolled",
            at = @At("RETURN")
    )
    private void mouseScrolled(double amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            MinecraftClient client = MinecraftClient.getInstance();
            double mouseX = client.mouse.getX()
                    * client.getWindow().getScaledWidth()
                    / client.getWindow().getWidth();
            double mouseY = client.mouse.getY()
                    * client.getWindow().getScaledHeight()
                    / client.getWindow().getHeight();
            int i = Math.min(this.suggestions.size(), parent.maxSuggestionSize);

            for (int l = 0; l < i; l++) {
                if (mouseX > this.area.getX()
                        && mouseX < this.area.getX() + this.area.getWidth()
                        && mouseY > this.area.getY() + 12 * l
                        && mouseY < this.area.getY() + 12 * l + 12) {
                    select(l + this.inWindowIndex);
                    break;
                }
            }
        }
    }
}
