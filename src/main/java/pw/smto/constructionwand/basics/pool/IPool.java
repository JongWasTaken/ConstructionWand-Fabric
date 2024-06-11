package pw.smto.constructionwand.basics.pool;

import org.jetbrains.annotations.Nullable;

public interface IPool<T>
{
    void add(T element);

    void remove(T element);

    @Nullable
    T draw();

    void reset();
}
