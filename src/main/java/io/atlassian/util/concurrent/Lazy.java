package io.atlassian.util.concurrent;

import static io.atlassian.util.concurrent.Timeout.getNanosTimeout;
import static io.atlassian.util.concurrent.Timeout.timeoutFactory;
import static io.atlassian.util.concurrent.Timeout.TimeSuppliers.NANOS;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Factory for creating lazily populated references.
 *
 * @since 2.1
 */
public final class Lazy {
  /**
   * Memoizing reference that is lazily computed using the supplied factory.
   * <p>
   * The {@link Supplier factory} is not held on to after it is used, enabling
   * it to be garbage collected.
   *
   * @param <T> the type.
   * @param factory for populating the initial value, called only once.
   * @return a supplier
   */
  public static <T> Supplier<T> supplier(final Supplier<T> factory) {
    return new Strong<>(factory);
  }

  /**
   * Memoizing reference that expires the specified amount of time after
   * creation.
   *
   * @param <T> the type.
   * @param factory for populating the initial value, called only once.
   * @param time the amount
   * @param unit the units the amount is in
   * @return a supplier
   */
  public static <T> Supplier<T> timeToLive(final Supplier<T> factory, final long time, final TimeUnit unit) {
    return new Expiring<>(factory, () -> new TimeToLive(getNanosTimeout(time, unit)));
  }

  /**
   * Memoizing reference that expires the specified amount of time after the
   * last time it was accessed.
   *
   * @param <T> the type.
   * @param factory for populating the initial value, called only once.
   * @param time the amount
   * @param unit the units the amount is in
   * @return a supplier
   */
  public static <T> Supplier<T> timeToIdle(final Supplier<T> factory, final long time, final TimeUnit unit) {
    return new Expiring<>(factory, () -> new TimeToIdle(timeoutFactory(time, unit, NANOS)));
  }

  /**
   * Returns a {@link io.atlassian.util.concurrent.ResettableLazyReference}
   * which creates the value by applying the provided
   * {@link java.util.function.Supplier}.
   *
   * @param supplier that creates the value that will be held by the
   * {@link io.atlassian.util.concurrent.ResettableLazyReference}.
   * @param <T> the type of the contained element.
   * @return a {@link io.atlassian.util.concurrent.ResettableLazyReference}
   * which creates the value by applying the provided
   * {@link java.util.function.Supplier}.
   * @since 3.0
   */
  public static <T> ResettableLazyReference<T> resettable(final Supplier<T> supplier) {
    return new ResettableLazyReference<T>() {
      @Override protected T create() throws Exception {
        return supplier.get();
      }
    };
  }

  //
  // inners
  //

  /**
   * Never expires.
   */
  static final class Strong<T> extends LazyReference<T> {
    // not private for testing
    volatile Supplier<T> supplier;

    Strong(final Supplier<T> supplier) {
      this.supplier = requireNonNull(supplier);
    }

    @Override protected T create() throws Exception {
      try {
        return supplier.get();
      } finally {
        supplier = null; // not needed any more
      }
    }
  }

  /**
   * Tracks timeout from construction time
   */
  static final class TimeToLive implements Predicate<Void> {
    private final Timeout timeout;

    TimeToLive(final Timeout timeout) {
      this.timeout = timeout;
    }

    @Override public boolean test(final Void input) {
      return !timeout.isExpired();
    }
  }

  /**
   * Tracks timeout since last time it was asked. Once timed-out it stays that
   * way.
   */
  static final class TimeToIdle implements Predicate<Void> {
    private volatile Timeout lastAccess;
    private final Supplier<Timeout> timeout;

    TimeToIdle(final Supplier<Timeout> timeout) {
      this.timeout = requireNonNull(timeout);
      lastAccess = timeout.get();
    }

    @Override public boolean test(final Void input) {
      final boolean alive = !lastAccess.isExpired();
      if (alive) {
        lastAccess = timeout.get();
      }
      return alive;
    }
  }
}
