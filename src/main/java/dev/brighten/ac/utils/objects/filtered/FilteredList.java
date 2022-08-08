package dev.brighten.ac.utils.objects.filtered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FilteredList<T> extends ArrayList<T> {

    private Predicate<T> predicate;

    public FilteredList(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean add(T t) {
        if(predicate.test(t)) {
            return super.add(t);
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        List<? extends T> filtered = c.stream().filter(predicate).collect(Collectors.toList());

        return super.addAll(filtered);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        List<? extends T> filtered = c.stream().filter(predicate).collect(Collectors.toList());

        return super.addAll(index, filtered);
    }

    @Override
    public void add(int index, T element) {
        if(predicate.test(element)) {
            super.add(index, element);
        }
    }

    @Override
    public T set(int index, T element) {
        if(predicate.test(element)) {
            return super.set(index, element);
        }
        return size() > index ? get(index) : null;
    }
}
