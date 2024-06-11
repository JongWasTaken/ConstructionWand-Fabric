package pw.smto.constructionwand;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import pw.smto.constructionwand.basics.option.WandOptions;
import pw.smto.constructionwand.client.ClientEvents;
import pw.smto.constructionwand.client.Network;
import pw.smto.constructionwand.client.RenderBlockPreview;
import pw.smto.constructionwand.items.wand.ItemWand;

import static pw.smto.constructionwand.Registry.Items.WANDS;

public class ConstructionWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        for(Item item : WANDS) {
            ModelPredicateProviderRegistry.register(
                    item, ConstructionWand.loc("using_core"),
                    (stack, world, entity, n) -> {
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
