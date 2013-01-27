/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util;

import java.util.Collection;
import javolution.lang.Functor;
import javolution.lang.Predicate;
import javolution.util.AbstractTable;
import javolution.util.FastComparator;
import javolution.util.FastTable;

/**
 * A reverse view over a table.
 */
public final class ReverseTableImpl<E> extends AbstractTable<E> {

    private final AbstractTable<E> that;

    public ReverseTableImpl(AbstractTable<E> that) {
        this.that = that;
    }
    
    @Override
    public int size() {
        return that.size();
    }

    @Override
    public E get(int index) {
        if ((index < 0) && (index >= size())) indexError(index);
        return that.get(size() - 1 - index);
    }

    @Override
    public E set(int index, E element) {
        if ((index < 0) && (index >= size())) indexError(index);
        return that.set(size() - 1 - index, element);
    }

    @Override
    public void add(int index, E element) {
        if ((index < 0) && (index > size())) indexError(index);
        that.add(size() - 1 - index, element);
    }

    @Override
    public E remove(int index) {
        if ((index < 0) && (index >= size())) indexError(index);
        return that.remove(size() - 1 - index);
    }

    @Override
    public ReverseTableImpl<E> copy() {
        return new ReverseTableImpl<E> (that.copy());
    }

    //
    // Non-abstract methods should forward to the actual table (unless impacted).
    //
    @Override
    public void clear() {
        that.clear();
    }

    @Override
    public E getFirst() {
        return that.getLast();
    }

    @Override
    public E getLast() {
        return that.getFirst();
    }

    @Override
    public boolean add(E element) {
        return that.add(element); // Add does not presuppose it is at the end. 
    }

    @Override
    public void addFirst(E element) {
        that.addLast(element);
    }

    @Override
    public void addLast(E element) {
        that.addFirst(element);
    }

    @Override
    public E removeFirst() {
        return that.removeLast();
    }

    @Override
    public E removeLast() {
        return that.removeFirst();
    }

    @Override
    public E pollFirst() {
        return that.pollLast();
    }

    @Override
    public E pollLast() {
        return that.pollFirst();
    }

    @Override
    public E peekFirst() {
        return that.peekLast();
    }

    @Override
    public E peekLast() {
        return that.peekFirst();
    }

    @Override
    public <R> FastTable<R> forEach(Functor<E, R> functor) {
        return that.forEach(functor);
    }

    @Override
    public void doWhile(Predicate<E> predicate) {
        that.doWhile(predicate);
    }

    @Override
    public boolean removeAll(Predicate<E> predicate) {
        return that.removeAll(predicate);
    }

    @Override
    public boolean addAll(final Collection<? extends E> elements) {
        return that.addAll(elements);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> elements) {
        return that.addAll(size() - 1 - index, elements);
    }

    @Override
    public boolean contains(E element) {
        return that.contains(element);
    }

    @Override
    public boolean remove(E element) {
        return that.remove(element);
    }

    @Override
    public int indexOf(E element) {
        return size() - 1 - that.lastIndexOf(element);
    }

    @Override
    public int lastIndexOf(E element) {
        return  size() - 1 - that.indexOf(element);
    }

    @Override
    public void sort() {
        super.sort();
    }

    @Override
    public FastComparator<E> comparator() {
        return that.comparator();
    }        
}
