package dev.smto.constructionwand.basics.option;

import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.api.IWandUpgrade;
import dev.smto.constructionwand.basics.ReplacementRegistry;
import dev.smto.constructionwand.items.core.CoreDefault;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class WandOptions
{
    public final NbtCompound tag;

    private static final String TAG_ROOT = "wand_options";

    public enum Lock
    {
        HORIZONTAL,
        VERTICAL,
        NORTHSOUTH,
        EASTWEST,
        NOLOCK
    }

    public enum Direction
    {
        TARGET,
        PLAYER
    }

    public enum Match
    {
        EXACT,
        SIMILAR,
        ANY
    }

    public final WandUpgradesSelectable<IWandCore> cores;

    public final OptionEnum<Lock> lock;
    public final OptionEnum<Direction> direction;
    public final OptionBoolean replace;
    public final OptionEnum<Match> match;
    public final OptionBoolean random;

    public final ItemStack stack;

    public final IOption<?>[] allOptions;

    public static WandOptions of(ItemStack wandStack) {
        return new WandOptions(wandStack);
    }

    private WandOptions(ItemStack wandStack) {
        tag = wandStack.getOrCreateSubNbt(TAG_ROOT);

        cores = new WandUpgradesSelectable<>(tag, "cores", new CoreDefault());

        lock = new OptionEnum<>(tag, "lock", Lock.class, Lock.NOLOCK);
        direction = new OptionEnum<>(tag, "direction", Direction.class, Direction.TARGET);
        replace = new OptionBoolean(tag, "replace", true);
        match = new OptionEnum<>(tag, "match", Match.class, Match.SIMILAR);
        random = new OptionBoolean(tag, "random", false);

        allOptions = new IOption[]{cores, lock, direction, replace, match, random};

        stack = wandStack;
    }

    @Nullable
    public IOption<?> get(String key) {
        for(IOption<?> option : allOptions) {
            if(option.getKey().equals(key)) return option;
        }
        return null;
    }

    public boolean testLock(Lock l) {
        if(lock.get() == Lock.NOLOCK) return true;
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

    public void writeToStack() {
        this.stack.setSubNbt(TAG_ROOT, tag);
    }

    public void writeToStack(ItemStack target) {
        target.setSubNbt(TAG_ROOT, tag);
    }
}
