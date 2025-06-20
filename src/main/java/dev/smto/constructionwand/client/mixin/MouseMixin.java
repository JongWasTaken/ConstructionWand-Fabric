package dev.smto.constructionwand.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
    @Redirect(at = @At(target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V", value = "INVOKE"), method = "onMouseScroll")
    private void onMouseScroll(PlayerInventory instance, double scrollAmount) {
        if (!dev.smto.constructionwand.client.ClientEvents.onScroll(scrollAmount)) {
            instance.scrollInHotbar(scrollAmount);
        }
    }
}