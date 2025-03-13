package pw.smto.constructionwand.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.Scroller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Redirect(at = @At(target = "Lnet/minecraft/client/input/Scroller;scrollCycling(DII)I", value = "INVOKE"), method = "onMouseScroll")
    private int onMouseScroll(double amount, int selectedIndex, int total) {
        if (!pw.smto.constructionwand.client.ClientEvents.onScroll(amount)) {
            return Scroller.scrollCycling(amount, selectedIndex, total);
        }
        return this.client.player.getInventory().getSelectedSlot();
    }
}
