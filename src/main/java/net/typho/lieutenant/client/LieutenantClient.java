package net.typho.lieutenant.client;

import com.mojang.brigadier.suggestion.Suggestion;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class LieutenantClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }

    public static boolean hasSuggestionIcon(Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());
        return id != null && Registries.ITEM.containsId(id);
    }

    public static void renderSuggestionIcon(DrawContext context, int x, int y, int mouseX, int mouseY, Suggestion suggestion) {
        Identifier id = Identifier.tryParse(suggestion.getText());

        if (id != null && Registries.ITEM.containsId(id)) {
            ItemStack stack = new ItemStack(Registries.ITEM.get(id));
            float scale = 2/3f;
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.scale(scale, scale, 1);
            matrices.translate(x / scale + (28 * (1 - scale)), y / scale + (28 * (1 - scale)), 0);
            context.drawItem(stack, 0, 0);
            matrices.pop();
        }
    }
}
