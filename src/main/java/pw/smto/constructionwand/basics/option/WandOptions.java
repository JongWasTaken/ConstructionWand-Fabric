package pw.smto.constructionwand.basics.option;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.Registry;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.basics.ReplacementRegistry;
import pw.smto.constructionwand.items.core.CoreDefault;
import pw.smto.constructionwand.items.core.ItemCoreAngel;
import pw.smto.constructionwand.items.core.ItemCoreDestruction;

public class WandOptions
{
    public final NbtCompound tag;

    private static final String TAG_ROOT = "wand_options";

    public enum LOCK
    {
        HORIZONTAL,
        VERTICAL,
        NORTHSOUTH,
        EASTWEST,
        NOLOCK
    }

    public enum DIRECTION
    {
        TARGET,
        PLAYER
    }

    public enum MATCH
    {
        EXACT,
        SIMILAR,
        ANY
    }

    public final WandUpgradesSelectable cores;

    public final OptionEnum<LOCK> lock;
    public final OptionEnum<DIRECTION> direction;
    public final OptionBoolean replace;
    public final OptionEnum<MATCH> match;
    public final OptionBoolean random;

    public final OptionInt used;

    public final IOption<?>[] allOptions;

    public WandOptions(ItemStack wandStack) {
        //tag = wandStack.getOrCreateSubNbt(TAG_ROOT);
        tag = wandStack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        cores = new WandUpgradesSelectable(tag, "cores", new CoreDefault());

        lock = new OptionEnum<>(tag, "lock", LOCK.class, LOCK.NOLOCK);
        direction = new OptionEnum<>(tag, "direction", DIRECTION.class, DIRECTION.TARGET);
        replace = new OptionBoolean(tag, "replace", true);
        match = new OptionEnum<>(tag, "match", MATCH.class, MATCH.SIMILAR);
        random = new OptionBoolean(tag, "random", false);

        used = new OptionInt(tag, "used", 0);

        allOptions = new IOption[]{cores, lock, direction, replace, match, random, used};
    }

    public ItemStack getIcon(IOption<?> option) {
        var key = option.get();
        if (key instanceof IWandCore core) {
            if (core instanceof ItemCoreDestruction) return Registry.Items.CORE_DESTRUCTION.getDefaultStack();
            if (core instanceof ItemCoreAngel) return Registry.Items.CORE_ANGEL.getDefaultStack();
            return Items.COBBLESTONE.getDefaultStack();
        }
        if (key instanceof LOCK lock) {
            switch (lock) {
                case HORIZONTAL:
                    return Items.OAK_SLAB.getDefaultStack();
                case VERTICAL:
                    return Items.OAK_FENCE.getDefaultStack();
                case NORTHSOUTH:
                    return Items.RAIL.getDefaultStack();
                case EASTWEST:
                    return Items.POWERED_RAIL.getDefaultStack();
            }
        }
        if (key instanceof DIRECTION direction) {
            return switch (direction) {
                case TARGET -> Items.OAK_LOG.getDefaultStack();
                case PLAYER -> Items.PLAYER_HEAD.getDefaultStack();
            };
        }
        if (key instanceof MATCH match) {
            switch (match) {
                case EXACT:
                    return Items.GRASS_BLOCK.getDefaultStack();
                case SIMILAR:
                    return Items.DIRT.getDefaultStack();
                case ANY:
                    return Items.PUMPKIN.getDefaultStack();
            }
        }
        if (key instanceof Boolean b) {
            if (option.getKey().equals("replace")) {
                if (b) return Items.WATER_BUCKET.getDefaultStack();
                return Items.BARRIER.getDefaultStack();
            }
            if (option.getKey().equals("random")) {
                if (b) return Items.COMMAND_BLOCK.getDefaultStack();
                return Items.BEDROCK.getDefaultStack();
            }
        }

        return Items.STICK.getDefaultStack();
    }

    @Nullable
    public IOption<?> get(String key) {
        for(IOption<?> option : allOptions) {
            if(option.getKey().equals(key)) return option;
        }
        return null;
    }

    public boolean testLock(LOCK l) {
        if(lock.get() == LOCK.NOLOCK) return true;
        return lock.get() == l;
    }

    public boolean matchBlocks(Block b1, Block b2) {
        switch(match.get()) {
            case EXACT:
                return b1 == b2;
            case SIMILAR:
                return ReplacementRegistry.matchBlocks(b1, b2);
            case ANY:
                return b1 != Blocks.AIR && b2 != Blocks.AIR;
        }
        return false;
    }

    public boolean hasUpgrade(IWandUpgrade upgrade) {
        if(upgrade instanceof IWandCore) return cores.hasUpgrade((IWandCore) upgrade);
        return false;
    }

    public boolean addUpgrade(IWandUpgrade upgrade) {
        if(upgrade instanceof IWandCore) return cores.addUpgrade((IWandCore) upgrade);
        return false;
    }

    public void writeToStack(ItemStack item) {
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
    }
}
