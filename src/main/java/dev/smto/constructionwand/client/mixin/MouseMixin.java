package dev.smto.constructionwand.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.ScrollWheelHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Redirect(at = @At(target = "Lnet/minecraft/client/ScrollWheelHandler;getNextScrollWheelSelection(DII)I", value = "INVOKE"), method = "onScroll")
    private int onMouseScroll(double amount, int selectedIndex, int total) {
        if (!dev.smto.constructionwand.client.ClientEvents.onScroll(amount)) {
            return ScrollWheelHandler.getNextScrollWheelSelection(amount, selectedIndex, total);
        }
        return this.minecraft.player.getInventory().getSelectedSlot();
    }
}
