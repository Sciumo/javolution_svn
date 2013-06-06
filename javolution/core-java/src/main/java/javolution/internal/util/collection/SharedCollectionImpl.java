/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javolution.util.function.Predicate;
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;

/**
 * A shared view over a collection allowing concurrent access and sequential updates.
 */
public class SharedCollectionImpl<E> implements CollectionService<E>,
        Serializable {

    protected final CollectionService<E> that;
    protected final Lock read;
    protected final Lock write;

    public SharedCollectionImpl(CollectionService<E> that) {
        this.that = that;
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.read = readWriteLock.readLock();
        this.write = readWriteLock.writeLock();
    }

    private SharedCollectionImpl(CollectionService<E> that, Lock read,
            Lock write) {
        this.that = that;
        this.read = read;
        this.write = write;
    }

    @Override
    public boolean add(E element) {
        write.lock();
        try {
            return that.add(element);
        } finally {
            write.unlock();
        }
    }

    @Override
    public boolean doWhile(Predicate<? super E> predicate) {
        read.lock();
        try {
            return that.doWhile(predicate);
        } finally {
            read.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> predicate) {
        write.lock();
        try {
            return that.removeIf(predicate);
        } finally {
            write.unlock();
        }
    }

    @Override
    @Deprecated
    public Iterator<E> iterator() {
        final Iterator<E> thatIterator = that.iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                read.lock();
                try {
                    return thatIterator.hasNext();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public E next() {
                read.lock();
                try {
                    return thatIterator.next();
                } finally {
                    read.unlock();
                }
            }

            @Override
            public void remove() {
                write.lock();
                try {
                    thatIterator.remove();
                } finally {
                    write.unlock();
                }
            }

        };
    }

    @Override
    public ComparatorService<? super E> comparator() {
        return that.comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionService<E>[] trySplit(int n) {
        CollectionService<E>[] tmp = that.trySplit(n);
        if (tmp == null)
            return null;
        SharedCollectionImpl<E>[] shareds = new SharedCollectionImpl[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            shareds[i] = new SharedCollectionImpl<E>(tmp[i], read, write);
        }
        return shareds;
    }

    private static final long serialVersionUID = 6737935331276281598L;
}
