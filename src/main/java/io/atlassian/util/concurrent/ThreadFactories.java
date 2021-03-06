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

package io.atlassian.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

/**
 * Factory for creating {@link java.util.concurrent.ThreadFactory} instances.
 * All factory implementations produce named threads to give good stack-traces.
 */
public class ThreadFactories {
  /**
   * Simple builder for {@link ThreadFactory} instances
   */
  public static final class Builder {
    String name;
    Type type = Type.USER;
    int priority = Thread.NORM_PRIORITY;
    Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

    Builder(final String name) {
      this.name = name;
    }

    public Builder name(final String name) {
      this.name = requireNonNull(name, "name");
      return this;
    }

    public Builder type(final Type type) {
      this.type = requireNonNull(type, "type");
      return this;
    }

    public Builder priority(final int priority) {
      this.priority = priority;
      return this;
    }

    public Builder uncaughtExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
      this.exceptionHandler = requireNonNull(exceptionHandler, "exceptionHandler");
      return this;
    }

    public ThreadFactory build() {
      return new Default(name, type, priority, exceptionHandler);
    }
  }

  public enum Type {
    DAEMON(true), USER(false);

    final boolean isDaemon;

    Type(final boolean isDaemon) {
      this.isDaemon = isDaemon;
    }
  }

  /**
   * Get a {@link io.atlassian.util.concurrent.ThreadFactories.Builder} with the
   * required name prefix.
   *
   * @param name threads will be named with this prefix
   * @return a {@link io.atlassian.util.concurrent.ThreadFactories.Builder} that
   * can specify the parameters for type, priority etc.
   */
  public static Builder named(final String name) {
    return new Builder(name);
  }

  /**
   * Get a {@link java.util.concurrent.ThreadFactory} with the required name
   * prefix. The produced threads are user threads and have normal priority.
   *
   * @param name the prefix to use for naming the threads.
   * @return a configured {@link java.util.concurrent.ThreadFactory}
   */
  public static ThreadFactory namedThreadFactory(@NotNull final String name) {
    return named(name).build();
  }

  /**
   * Get a {@link java.util.concurrent.ThreadFactory} with the required name
   * prefix and type (user or daemon). The produced threads have normal
   * priority.
   *
   * @param name the prefix to use for naming the threads.
   * @param type whether they are User or Daemon threads.
   * @return a configured {@link java.util.concurrent.ThreadFactory}
   */
  public static ThreadFactory namedThreadFactory(@NotNull final String name, @NotNull final Type type) {
    return named(name).type(type).build();
  }

  /**
   * Get a {@link java.util.concurrent.ThreadFactory} with the required name
   * prefix, type and priority.
   *
   * @param name the prefix to use for naming the threads.
   * @param type whether they are User or Daemon threads.
   * @param priority the thread priority, must not be lower than
   * {@link java.lang.Thread#MIN_PRIORITY} or greater than
   * {@link java.lang.Thread#MAX_PRIORITY}
   * @return a configured {@link java.util.concurrent.ThreadFactory}
   */
  public static ThreadFactory namedThreadFactory(@NotNull final String name, @NotNull final Type type, final int priority) {
    return named(name).type(type).priority(priority).build();
  }

  // /CLOVER:OFF
  private ThreadFactories() {
    throw new AssertionError("cannot instantiate!");
  }

  // /CLOVER:ON

  static class Default implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;
    final Type type;
    final int priority;
    final UncaughtExceptionHandler exceptionHandler;

    Default(final String name, final Type type, final int priority, final UncaughtExceptionHandler exceptionHandler) {
      namePrefix = requireNonNull(name, "name") + ":thread-";
      this.type = requireNonNull(type, "type");
      if (!(priority >= Thread.MIN_PRIORITY)) {
        throw new IllegalArgumentException("priority too low");
      }
      if (!(priority <= Thread.MAX_PRIORITY)) {
        throw new IllegalArgumentException("priority too high");
      }
      this.priority = priority;
      final SecurityManager securityManager = System.getSecurityManager();
      final ThreadGroup parent = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
      group = new ThreadGroup(parent, name);
      this.exceptionHandler = exceptionHandler;
    }

    public Thread newThread(final Runnable r) {
      final String name = namePrefix + threadNumber.getAndIncrement();
      final Thread t = new Thread(group, r, name, 0);
      t.setDaemon(type.isDaemon);
      t.setPriority(priority);
      t.setUncaughtExceptionHandler(exceptionHandler);
      return t;
    }
  }
}
