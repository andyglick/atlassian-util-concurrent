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

import java.util.concurrent.TimeUnit;

/**
 * Something that can be awaited upon.
 * 
 * @author Jed Wesley-Smith
 */
public interface Awaitable {

  /**
   * Await for the condition to become true.
   * 
   * @throws {@link InterruptedException} if {@link Thread#interrupt()
   * interrupted}
   */
  void await() throws InterruptedException;

  /**
   * Await for the specified time for the condition to become true.
   * 
   * @param time the amount to wait.
   * @param unit the unit to wait in.
   * @throws {@link InterruptedException} if {@link Thread#interrupt()
   * interrupted}
   * @return true if the condition became true within the time limit, false
   * otherwise.
   */
  boolean await(long time, TimeUnit unit) throws InterruptedException;
}
