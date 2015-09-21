/**
 * Copyright 2011 Atlassian Pty Ltd 
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

/**
 * Contains the a side-effect that may be applied
 *
 * @param <A>
 *
 * @since 2.4
 */
@FunctionalInterface public interface Effect<A> {
  /**
   * Perform the side-effect.
   */
  void apply(A a);

  /**
   * A thing upon which side-effects may be applied.
   * 
   * @param <A> the type of thing to supply to the effect.
   */
  @FunctionalInterface interface Applicant<A> {
    /**
     * Perform the given side-effect for each contained element.
     */
    void foreach(Effect<A> effect);
  }
}
