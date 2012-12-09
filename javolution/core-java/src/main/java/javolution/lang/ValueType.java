/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

/**
 * <p> This interface identifies objects which can be manipulated by 
 *     value; a JVM implementation may allocate instances of this class 
 *     on the stack and pass references by copy.</p>
 *     
 * <p> {@link ValueType} instances are {@link Copyable} to be easily 
 *     exported outside a stack when  executing in 
 *     {@link javolution.context.StackContext StackContext}.
 *     [code]
 *     public final class Complex implements ValueType<Complex> { ... }
 *     ...
 *     public Complex sumOf(Complex[] values) {
 *         StackContext.enter(); // Starts stack allocation.
 *         try {
 *             Complex sum = Complex.ZERO;
 *             for (Complex c : values) {
 *                 sum = sum.plus(c);
 *             }
 *             return StackContext.outerCopy(sum); // Exports result outside of the stack.
 *         } finally {
 *             StackContext.exit(); // Resets stacks.
 *         }
 *     }[/code]</p>
 *      
 * <p> <b>Note:</b> "Stack" allocation is not the only optimization that a VM 
 *     can do on {@link ValueType}. The VM might decide not to perform any 
 *     allocation at all and store values directly in registers.</p> 
 *             
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public interface ValueType<T extends ValueType> extends Immutable, Copyable<T> {
    
}