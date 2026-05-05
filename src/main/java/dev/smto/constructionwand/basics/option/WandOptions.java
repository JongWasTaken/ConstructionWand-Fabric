package dev.smto.constructionwand.basics.option;

import dev.smto.constructionwand.api.IWandCore;
import dev.smto.constructionwand.api.IWandUpgrade;
import dev.smto.constructionwand.basics.ReplacementRegistry;
import dev.smto.constructionwand.items.core.CoreDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WandOptions {
    public final CompoundTag tag;
    public final CustomModelData modelData;

    public enum Lock {
        HORIZONTAL,
        VERTICAL,
        NORTHSOUTH,
        EASTWEST,
        NOLOCK
    }

    public enum Direction {
        TARGET,
        PLAYER
    }

    public enum Match {
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
        this.tag = wandStack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        this.modelData = wandStack.getComponents().getOrDefault(DataComponents.CUSTOM_MODEL_DATA, CustomModelData.EMPTY);

        this.cores = new WandUpgradesSelectable<>(this.tag, "cores", new CoreDefault());

        this.lock = new OptionEnum<>(this.tag, "lock", Lock.class, Lock.NOLOCK);
        this.direction = new OptionEnum<>(this.tag, "direction", Direction.class, Direction.TARGET);
        this.replace = new OptionBoolean(this.tag, "replace", true);
        this.match = new OptionEnum<>(this.tag, "match", Match.class, Match.SIMILAR);
        this.random = new OptionBoolean(this.tag, "random", false);

        this.allOptions = new IOption[]{this.cores, this.lock, this.direction, this.replace, this.match, this.random};

        this.stack = wandStack;
    }

    @Nullable
    public IOption<?> get(String key) {
        for (IOption<?> option : this.allOptions) {
            if (option.getKey().equals(key)) return option;
        }
        return null;
    }

    public boolean testLock(Lock l) {
        if (this.lock.get() == Lock.NOLOCK) return true;
        return this.lock.get() == l;
    }

    public boolean matchBlocks(Block b1, Block b2) {
        return switch (this.match.get()) {
            case EXACT -> b1 == b2;
            case SIMILAR -> ReplacementRegistry.matchBlocks(b1, b2);
            case ANY -> b1 != Blocks.AIR && b2 != Blocks.AIR;
        };
    }

    public boolean hasUpgrade(IWandUpgrade upgrade) {
        if (upgrade instanceof IWandCore) return this.cores.hasUpgrade((IWandCore) upgrade);
        return false;
    }

    public boolean addUpgrade(IWandUpgrade upgrade) {
        if (upgrade instanceof IWandCore) return this.cores.addUpgrade((IWandCore) upgrade);
        return false;
    }

    // 1.21.4 introduces new item models, which are cool on paper but kinda useless in this case,
    // because components cannot be used to match the model yet.
    // So we use custom model data instead.
    // Cant wait to once again rewrite this for 1.21.5!
    // Update: it is 26.1.2 now and i still have not rewritten this because it works well enough lmao

    public void writeToStack() {
        this.stack.set(DataComponents.CUSTOM_DATA, CustomData.of(this.tag));
        this.stack.set(DataComponents.CUSTOM_MODEL_DATA, this.mutateModelData());
    }

    public void writeToStack(ItemStack target) {
        target.set(DataComponents.CUSTOM_DATA, CustomData.of(this.tag));
        this.stack.set(DataComponents.CUSTOM_MODEL_DATA, this.mutateModelData());
    }

    private CustomModelData mutateModelData() {
        return new CustomModelData(this.modelData.floats(), List.of(!(this.cores.get() instanceof CoreDefault)), this.modelData.strings(), List.of(this.cores.get().getColor()));
    }
}
