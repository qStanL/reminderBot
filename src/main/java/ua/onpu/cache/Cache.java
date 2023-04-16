package ua.onpu.cache;

import java.util.List;

public interface Cache<T> {
    void add(T t);

    T findBy(Long id);

    List<T> getAll();
}