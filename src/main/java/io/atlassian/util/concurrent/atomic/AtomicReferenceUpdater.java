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

package io.atlassian.util.concurrent.atomic;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <strong>Experimental</strong>. Please note this class is experimental and may
 * be removed in later versions unless you <i>really, really</i> like it. If you
 * do, mail me, jed@atlassian.com
 * <p>
 * Implements the logic for updating an
 * {@link java.util.concurrent.atomic.AtomicReference} correctly, using the
 * current value, computing the update and then setting it if it hasn't changed
 * in the meantime.
 * <p>
 *
 * @param <T> the type of the reference.
 * @since 0.0.12
 */
public abstract class AtomicReferenceUpdater<T> implements Function<T, T> {
  private final AtomicReference<T> reference;

  /**
   * <p>
   * Constructor for AtomicReferenceUpdater.
   * </p>
   *
   * @param reference a {@link java.util.concurrent.atomic.AtomicReference}
   * object.
   */
  public AtomicReferenceUpdater(final AtomicReference<T> reference) {
    this.reference = requireNonNull(reference, "reference");
  }

  /**
   * Do the actual update. Calls the factory method with the old value to do the
   * update logic, then sets the value to that if it hasn't changed in the
   * meantime.
   *
   * @return the new updated value.
   */
  public final T update() {
    T oldValue, newValue;
    do {
      oldValue = reference.get();
      newValue = apply(oldValue);
      // test first to implement TTAS
      // then compare and set
    } while ((reference.get() != oldValue) || !reference.compareAndSet(oldValue, newValue));
    return newValue;
  }
}
