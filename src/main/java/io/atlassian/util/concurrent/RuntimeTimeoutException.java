/**
 * Copyright 2010 Atlassian Pty Ltd 
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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Convenience class for re-throwing
 * {@link java.util.concurrent.TimeoutException} as an unchecked exception.
 */
public class RuntimeTimeoutException extends RuntimeException {

  private static final long serialVersionUID = -5025209597479375477L;

  /**
   * Constructor for RuntimeTimeoutException.
   *
   * @param cause a {@link java.util.concurrent.TimeoutException}.
   */
  public RuntimeTimeoutException(final TimeoutException cause) {
    super(requireNonNull(cause, "cause"));
  }

  /**
   * Constructor for RuntimeTimeoutException.
   *
   * @param message a {@link java.lang.String} object.
   * @param cause a {@link java.util.concurrent.TimeoutException}.
   */
  public RuntimeTimeoutException(final String message, final TimeoutException cause) {
    super(message, requireNonNull(cause, "cause"));
  }

  /**
   * Constructor for RuntimeTimeoutException.
   *
   * @param time a long.
   * @param unit a {@link java.util.concurrent.TimeUnit}.
   */
  public RuntimeTimeoutException(final long time, final TimeUnit unit) {
    super(new TimedOutException(time, unit));
  }

  /** {@inheritDoc} */
  @Override public TimeoutException getCause() {
    return (TimeoutException) super.getCause();
  }
}
