package dev.smto.constructionwand.basics;

import dev.smto.constructionwand.ConstructionWand;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Set;

public class ReplacementRegistry {
    private static final HashSet<HashSet<Item>> REPLACEMENTS = new HashSet<>();

    public static void init() {
        ReplacementRegistry.REPLACEMENTS.clear();

        for (String key : ConstructionWand.Config.similarBlocks) {
            if (!(key instanceof String)) continue;
            HashSet<Item> set = new HashSet<>();

            for (String id : key.split(";")) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
                if (item == Items.AIR) {
                    ConstructionWand.LOGGER.warn("Replacement Registry: Could not find item {}", id);
                    continue;
                }
                set.add(item);
            }
            if (!set.isEmpty()) ReplacementRegistry.REPLACEMENTS.add(set);
        }
    }

    public static Set<Item> getMatchingSet(Item item) {
        HashSet<Item> res = new HashSet<>();

        for (HashSet<Item> set : ReplacementRegistry.REPLACEMENTS) {
            if (set.contains(item)) res.addAll(set);
        }
        res.remove(item);
        return res;
    }

    public static boolean matchBlocks(Block b1, Block b2) {
        if (b1 == b2) return true;
        if (b1 == Blocks.AIR || b2 == Blocks.AIR) return false;

        for (HashSet<Item> set : ReplacementRegistry.REPLACEMENTS) {
            if (set.contains(b1.asItem()) && set.contains(b2.asItem())) return true;
        }
        return false;
    }
}
