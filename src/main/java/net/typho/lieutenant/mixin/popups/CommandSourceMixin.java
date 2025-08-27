package net.typho.lieutenant.mixin.popups;

import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandSource.class)
public interface CommandSourceMixin {
    @Redirect(
            method = "forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"
            )
    )
    private static boolean equals(String instance, Object o) {
        return true;
    }

    /**
     * @author The Typhothanian
     * @reason Better command suggestions
     */
    @Overwrite
    static boolean shouldSuggest(String remaining, String candidate) {
        return candidate.contains(remaining);
    }
}
