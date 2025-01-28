package pw.smto.constructionwand.basics.option;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import pw.smto.constructionwand.api.IWandCore;
import pw.smto.constructionwand.api.IWandUpgrade;
import pw.smto.constructionwand.basics.ReplacementRegistry;
import pw.smto.constructionwand.items.core.CoreDefault;

import java.util.List;

public class WandOptions
{
    public final NbtCompound tag;
    public final CustomModelDataComponent modelData;

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
        //tag = wandStack.getOrCreateSubNbt(TAG_ROOT);
        tag = wandStack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        modelData = wandStack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);

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

    // 1.21.4 introduces new item models, which are cool on paper but kinda useless in this case,
    // because components cannot be used to match the model yet.
    // So we use custom model data instead.
    // Cant wait to once again rewrite this for 1.21.5!

    public void writeToStack() {
        this.stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        this.stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, mutateModelData());
    }

    public void writeToStack(ItemStack target) {
        target.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        this.stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, mutateModelData());
    }

    private CustomModelDataComponent mutateModelData() {
        return new CustomModelDataComponent(modelData.floats(), List.of(!(cores.get() instanceof CoreDefault)), modelData.strings(), List.of(cores.get().getColor()));
    }
}
