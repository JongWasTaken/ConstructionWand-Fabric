package dev.smto.constructionwand.basics.pool;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class OrderedPool<T> implements IPool<T> {
    private final ArrayList<T> elements;
    private int index;

    public OrderedPool() {
        this.elements = new ArrayList<>();
        this.reset();
    }

    @Override
    public void add(T element) {
        this.elements.add(element);
    }

    @Override
    public void remove(T element) {
        this.elements.remove(element);
    }

    @Nullable
    @Override
    public T draw() {
        if (this.index >= this.elements.size()) return null;
        T e = this.elements.get(this.index);
        this.index++;
        return e;
    }

    @Override
    public void reset() {
        this.index = 0;
    }
}
