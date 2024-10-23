package com.suspend.mapping.fetching;

import java.util.*;

public class Bag<E> implements List<E> {
    protected List<E> bag = new ArrayList<>();

    @Override
    public E get(int index) {
        return bag.get(index);
    }

    @Override
    public int size() {
        return bag.size();
    }

    @Override
    public boolean add(E e) {
        return bag.add(e);
    }

    @Override
    public void add(int index, E element) {
        bag.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return bag.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return bag.addAll(index, c);
    }

    @Override
    public void clear() {
        bag.clear();
    }

    @Override
    public boolean contains(Object o) {
        return bag.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return bag.containsAll(c);
    }

    @Override
    public int indexOf(Object o) {
        return bag.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return bag.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return bag.iterator();
    }

    @Override
    public Object[] toArray() {
        return bag.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        if (ts.length < bag.size()) {
            return (T[]) Arrays.copyOf(bag.toArray(), bag.size(), ts.getClass());
        }

        System.arraycopy(bag.toArray(), 0, ts, 0, bag.size());
        if (ts.length > bag.size()) {
            ts[bag.size()] = null;
        }
        return ts;
    }

    @Override
    public int lastIndexOf(Object o) {
        return bag.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return bag.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return bag.listIterator(index);
    }

    @Override
    public boolean remove(Object o) {
        return bag.remove(o);
    }

    @Override
    public E remove(int index) {
        return bag.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return bag.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return bag.retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        return bag.set(index, element);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return bag.subList(fromIndex, toIndex);
    }

//    public List<E> getBag() {
//        return bag;
//    }
}
