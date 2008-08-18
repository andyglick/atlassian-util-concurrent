package com.atlassian.util.concurrent;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The class to which this annotation is applied is thread-safe. This means that
 * no sequences of accesses (reads and writes to public fields, calls to public
 * methods) may put the object into an invalid state, regardless of the
 * interleaving of those actions by the runtime, and without requiring any
 * additional synchronization or coordination on the part of the caller.
 * 
 * @see http
 * ://www.javaconcurrencyinpractice.com/annotations/doc/net/jcip/annotations
 * /ThreadSafe.html
 */
@Documented
@Target(value = TYPE)
@Retention(value = RUNTIME)
public @interface ThreadSafe {

}
