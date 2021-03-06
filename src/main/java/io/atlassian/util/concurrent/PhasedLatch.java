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

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import net.jcip.annotations.ThreadSafe;

/**
 * A {@link io.atlassian.util.concurrent.PhasedLatch} is a shared latch that
 * resets after it is released and can be reused. Potentially waiting threads
 * can test the current phase before performing an action. The action is then
 * guarded by that phase and can await that phase to be advanced via a call to
 * {@link #release() release} the current phase.
 * <p>
 * This class is not final but cannot be sub-classed due to the private ctor.
 */
@ThreadSafe public class PhasedLatch implements ReusableLatch {
  private static final PhaseComparator comparator = new PhaseComparator();

  private final Sync sync = new Sync();

  PhasedLatch() {}

  /**
   * Release the current phase.
   */
  public final void release() {
    sync.releaseShared(1);
  }

  /**
   * Await the current phase.
   *
   * @throws java.lang.InterruptedException if interrupted
   */
  public final void await() throws InterruptedException {
    awaitPhase(getPhase());
  }

  /**
   * {@inheritDoc}
   *
   * Await the current phase for the specified period.
   */
  public final boolean await(final long time, final TimeUnit unit) throws InterruptedException {
    return sync.tryAcquireSharedNanos(getPhase(), unit.toNanos(time));
  }

  /**
   * Await the specified phase.
   *
   * @param phase the phase to wait for
   * @throws java.lang.InterruptedException if interrupted
   */
  public final void awaitPhase(final int phase) throws InterruptedException {
    sync.acquireSharedInterruptibly(phase);
  }

  /**
   * Await the specified phase for the specified period.
   *
   * @param phase the phase to wait for
   * @param period the period of time to wait for, as specified by:
   * @param unit of time to measure the period in
   * @return true if the phase was passed, false otherwise
   * @throws java.lang.InterruptedException if interrupted
   */
  public final boolean awaitPhase(final int phase, final long period, final TimeUnit unit) throws InterruptedException {
    return sync.tryAcquireSharedNanos(phase, unit.toNanos(period));
  }

  /**
   * Get the current phase of the latch.
   *
   * @return a int.
   */
  public int getPhase() {
    return sync.getCurrentPhase();
  }

  /**
   * This sync implements Phasing. The state represents the current phase as an
   * integer that continually increases. The phase can wrap around past
   * {@link Integer#MAX_VALUE}
   */
  private static class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = -7753362916930221487L;

    public int getCurrentPhase() {
      return getState();
    }

    @Override protected int tryAcquireShared(final int phase) {
      return comparator.isPassed(getState(), phase) ? 1 : -1;
    }

    @Override protected boolean tryReleaseShared(final int ignore) {
      while (true) {
        final int state = getState();
        // /CLOVER:OFF
        // cannot test CAS
        if (compareAndSetState(state, state + 1)) {
          return true;
        }
        // /CLOVER:ON
      }
    }
  }

  static class PhaseComparator implements Comparator<Integer>, Serializable {
    private static final long serialVersionUID = -614957178717195674L;

    public int compare(final Integer current, final Integer waitingFor) {
      return waitingFor - current;
    };

    /**
     * Has the current phase passed the waiting phase.
     * 
     * @param current value of the current phase
     * @param waitingFor value of the desired phase
     * @return true if current is greater than waiting
     */
    boolean isPassed(final int current, final int waitingFor) {
      return compare(current, waitingFor) < 0;
    }
  }
}
