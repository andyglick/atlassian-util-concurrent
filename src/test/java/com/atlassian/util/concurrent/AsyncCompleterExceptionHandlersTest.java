package com.atlassian.util.concurrent;

import static org.junit.Assert.assertNull;

import com.atlassian.util.concurrent.AsyncCompleter.Exceptions;

import org.junit.Test;

import com.google.common.base.Supplier;

public class AsyncCompleterExceptionHandlersTest {
    @Test(expected = TestException.class)
    public void exceptionsThrow() {
        exception(Exceptions.THROW);
    }

    @Test
    public void exceptionsIgnored() {
        assertNull(exception(Exceptions.IGNORE_EXCEPTIONS));
    }

    private Object exception(final Exceptions handler) {
        final Supplier<Object> wrapped = handler.handler().apply(new Supplier<Object>() {
            public Object get() {
                throw new TestException();
            }
        });
        final Object result = wrapped.get();
        return result;
    }

    public class TestException extends RuntimeException {}
}