package pw.smto.constructionwand;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.client.ClientEvents;
import pw.smto.constructionwand.client.Network;
import pw.smto.constructionwand.items.wand.ItemWand;

import static pw.smto.constructionwand.Registry.Items.WANDS;

@Environment(EnvType.CLIENT)
public class ConstructionWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for(Item item : WANDS) {
            ModelPredicateProviderRegistry.register(
                    item, ConstructionWand.loc("using_core"),
                    (stack, world, entity, seed) -> {
                        if (entity == null || !(stack.getItem() instanceof ItemWand)) return 0;
                        return new WandOptions(stack).cores.get().getColor() > -1 ? 1 : 0;
                    }
            );
            ColorProviderRegistry.ITEM.register((stack, layer) -> (layer == 1 && stack.getItem() instanceof ItemWand) ?
                    new WandOptions(stack).cores.get().getColor() : -1, item);
        }
        Network.init();
        ClientEvents.init();
    }
}
