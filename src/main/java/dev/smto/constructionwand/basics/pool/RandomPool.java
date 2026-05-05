package dev.smto.constructionwand.basics.pool;

import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class RandomPool<T> implements IPool<T> {
    private final RandomSource rng;
    private final HashMap<T, Integer> elements;
    private HashSet<T> pool;

    public RandomPool(RandomSource rng) {
        this.rng = rng;
        this.elements = new HashMap<>();
        this.reset();
    }

    @Override
    public void add(T element) {
        this.addWithWeight(element, 1);
    }

    @Override
    public void remove(T element) {
        this.elements.remove(element);
        this.pool.remove(element);
    }

    public void addWithWeight(T element, int weight) {
        if (weight < 1) return;
        this.elements.merge(element, weight, Integer::sum);
        this.pool.add(element);
    }

    @Nullable
    @Override
    public T draw() {
        int allWeights = this.pool.stream().reduce(0, (partialRes, e) -> partialRes + this.elements.get(e), Integer::sum);
        if (allWeights < 1) return null;

        int random = this.rng.nextInt(allWeights);
        int accWeight = 0;

        for (T e : this.pool) {
            accWeight += this.elements.get(e);
            if (random < accWeight) {
                this.pool.remove(e);
                return e;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        this.pool = new HashSet<>(this.elements.keySet());
    }
}
