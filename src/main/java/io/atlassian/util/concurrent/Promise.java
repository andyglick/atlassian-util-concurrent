/**
 * Copyright 2012 Atlassian Pty Ltd 
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

import javax.annotation.Nonnull;
import java.util.concurrent.Future;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A promise that presents a nicer interface to
 * {@link java.util.concurrent.Future}. It can be claimed without needing to
 * catch checked exceptions, and it may be mapped to new types of Promise via
 * the {@link #map(Function)} and {@link #flatMap(Function)} methods.
 * <p>
 * For instance, if you have a <code>Promise&lt;A&gt;</code> and you want to do
 * some operation on the value (an A) you can use {@link #map(Function)} to turn
 * this into a Promise of some other type. Let's say you get back a
 * <code>Person</code> and you really only need their surname:
 *
 * <pre>
 * public Promise&lt;String&gt; fetchSurname(PersonId id) {
 *   Promise&lt;Person&gt; promise asyncClient.fetchPerson(id);
 *   return promise.map(new Function&lt;Person, String&gt;() {
 *     public String apply(Person p) {
 *       return p.surname();
 *     }
 *   };
 * }
 * </pre>
 * <p>
 * If you want to do some further asynchronous operation using the value, you
 * can use {@link #flatMap(Function)} to turn this into a Promise of some other
 * type. Let's say you get back a <code>Person</code> and you really only need
 * to perform a further query to get their address:
 *
 * <pre>
 * public Promise&lt;Address&gt; fetchAddress(PersonId id) {
 *   Promise&lt;Person&gt; promise asyncClient.fetchPerson(id);
 *   return promise.flatMap(new Function&lt;Person, Promise&lt;Address&gt;&gt;() {
 *     public Promise&lt;Address&gt; apply(Person p) {
 *       return asyncClient.fetchAddress(p.addressId());
 *     }
 *   };
 * }
 * </pre>
 * <p>
 * Note that there are a number of handy utility functions for creating
 * <code>Promise</code> objects on the
 * {@link io.atlassian.util.concurrent.Promises} companion.
 * <p>
 * Cancelling a Promise that hasn't yet been completed will do it with a
 * {@link java.util.concurrent.CancellationException} and that will propagate to
 * dependent Promises. But cancelling a dependent Promise will not cancel the
 * original one.
 *
 * @since 2.4
 */
public interface Promise<A> extends Future<A> {
  /**
   * Blocks the thread waiting for a result. Exceptions are thrown as runtime
   * exceptions.
   *
   * @return The promised object
   */
  A claim();

  /**
   * Registers a callback to be called when the promised object is available.
   * May not be executed in the same thread as the caller.
   * 
   * @param c The consumer to call with the result
   * @return This object for chaining
   */
  Promise<A> done(Consumer<? super A> c);

  /**
   * Registers a callback to be called when an exception is thrown. May not be
   * executed in the same thread as the caller.
   * 
   * @param c The consumer to call with the throwable
   * @return This object for chaining
   */
  Promise<A> fail(Consumer<Throwable> c);

  /**
   * Registers a TryConsumer to handle both success and failure (exception)
   * cases. May not be executed in the same thread as the caller.
   * <p>
   * See {@link Promises#compose(Consumer, Consumer)}
   *
   * @param callback The future callback
   * @return This object for chaining
   */
  Promise<A> then(TryConsumer<? super A> callback);

  /**
   * Transforms this {@link io.atlassian.util.concurrent.Promise} from one type
   * to another by way of a transformation function.
   * <p>
   *
   * @param function The transformation function
   * @return A new promise resulting from the transformation
   * @param <B> a B.
   */
  <B> Promise<B> map(Function<? super A, ? extends B> function);

  /**
   * Transforms this promise from one type to another by way of a transformation
   * function that returns a new Promise, leaving the strategy for that promise
   * production up to the function.
   * <p>
   * Note this is known as flatMap as it first maps to a
   * <code>Promise&lt;Promise&lt;A&gt;&gt;</code> and then flattens that out
   * into a single layer Promise.
   *
   * @param function The transformation function to a new Promise value
   * @return A new promise resulting from the transformation
   * @param <B> a B.
   */
  <B> Promise<B> flatMap(Function<? super A, ? extends Promise<? extends B>> function);

  /**
   * Recover from an exception using the supplied exception strategy
   *
   * @param handleThrowable rehabilitate the exception with a value of type B
   * @return A new promise that will not throw an exception (unless
   * handleThrowable itself threw).
   */
  Promise<A> recover(Function<Throwable, ? extends A> handleThrowable);

  /**
   * Transform this promise from one type to another, also providing a strategy
   * for dealing with any exceptions encountered.
   *
   * @param handleThrowable rehabilitate the exception with a value of type B
   * @param function mapping function
   * @return A new promise resulting from the catamorphic transformation. This
   * promise will not throw an exception (unless handleThrowable itself threw).
   * @param <B> a B.
   */
  <B> Promise<B> fold(Function<Throwable, ? extends B> handleThrowable, Function<? super A, ? extends B> function);

  /**
   * Consumer interface to be called after a promise is fulfilled with a
   * succesful value or a failure.
   * 
   * @param <A> type of the successful value.
   */
  interface TryConsumer<A> extends Consumer<A> {
    void fail(@Nonnull Throwable t);
  }
}
