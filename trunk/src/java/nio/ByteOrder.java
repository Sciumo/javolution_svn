/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package java.nio;

/**
 * Clean-room implementation of ByteOrder to support 
 * <code>javolution.util.Struct</code> when <code>java.nio</code> is
 * not available.
 */
public final class ByteOrder {
    public static final ByteOrder BIG_ENDIAN = new ByteOrder();

    public static final ByteOrder LITTLE_ENDIAN = new ByteOrder();

    public static ByteOrder nativeOrder() {
        throw new UnsupportedOperationException();
    }

}