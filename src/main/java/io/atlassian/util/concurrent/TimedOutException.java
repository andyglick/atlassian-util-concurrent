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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Convenience exception that takes a time and a unit and produces a meaningful
 * error message.
 */
public class TimedOutException extends TimeoutException {
  private static final long serialVersionUID = 2639693125779305458L;

  /**
   * Constructor for TimedOutException.
   *
   * @param time a long.
   * @param unit a {@link java.util.concurrent.TimeUnit} object.
   */
  public TimedOutException(final long time, final TimeUnit unit) {
    super("Timed out after: " + time + " " + unit);
  }
}
