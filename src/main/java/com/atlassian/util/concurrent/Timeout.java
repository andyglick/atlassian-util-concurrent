/**
 * Copyright 2008 Atlassian Pty Ltd 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.atlassian.util.concurrent;

import net.jcip.annotations.Immutable;

import java.util.concurrent.TimeUnit;

/**
 * Used to calculate timeouts from when it is created when successively calling blocking methods.
 * Always converts to nanoseconds.
 * <p>
 * Usage:
 * 
 * <pre>
 * 
 * </pre>
 */
@Immutable public class Timeout {

    private static final TimeSupplier NANO_SUPPLIER = new TimeSupplier() {
        public long currentTime() {
            return System.nanoTime();
        };

        public TimeUnit precision() {
            return TimeUnit.NANOSECONDS;
        };
    };

    private static final TimeSupplier MILLIS_SUPPLIER = new TimeSupplier() {
        public long currentTime() {
            return System.currentTimeMillis();
        };

        public TimeUnit precision() {
            return TimeUnit.MILLISECONDS;
        };
    };

    /**
     * Get a {@link Timeout} that uses nanosecond precision. The accuracy will depend on the
     * accuracy of {@link System#nanoTime()}.
     * 
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the <tt>time</tt> argument.
     * @return timeout with {@link TimeUnit#NANOSECONDS} precision.
     */
    public static Timeout getNanosTimeout(final long time, final TimeUnit unit) {
        return new Timeout(time, unit, NANO_SUPPLIER);
    }

    /**
     * Get a {@link Timeout} that uses nanosecond precision. The accuracy will depend on the
     * accuracy of {@link System#nanoTime()}.
     * 
     * @param time the maximum time to wait for the lock
     * @param unit the time unit of the <tt>time</tt> argument.
     * @return timeout with {@link TimeUnit#NANOSECONDS} precision.
     */
    public static Timeout getMillisTimeout(final long time, final TimeUnit unit) {
        return new Timeout(time, unit, MILLIS_SUPPLIER);
    }

    private final long created;
    private final long time;
    private final TimeSupplier supplier;

    Timeout(final long time, final TimeUnit unit, final TimeSupplier supplier) {
        created = supplier.currentTime();
        this.supplier = supplier;
        this.time = this.supplier.precision().convert(time, unit);
    }

    public long getTime() {
        return (created + time) - supplier.currentTime();
    }

    public TimeUnit getUnit() {
        return supplier.precision();
    }

    /**
     * Supply time and precision to a {@link Timeout}.
     */
    interface TimeSupplier {
        long currentTime();

        TimeUnit precision();
    }
}