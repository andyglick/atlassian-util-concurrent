package io.atlassian.util.concurrent;

import io.atlassian.util.concurrent.atomic.AtomicReference;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import static io.atlassian.util.concurrent.Promises.compose;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class) public class PromisesTest {

  @Mock private Consumer<Object> done;
  @Mock private Consumer<Throwable> fail;
  @Mock private Promise.TryConsumer<Object> futureConsumer;

  @Test public void promiseValue() {
    final Object instance = new Object();
    final Promise<Object> promise = Promises.forCompletionStage(CompletableFuture.completedFuture(instance));

    assertThat(promise.isDone(), is(true));
    assertThat(promise.isCancelled(), is(false));
    assertThat(promise.claim(), is(instance));

    promise.then(compose(done, fail));
    verify(done).accept(instance);
    verifyZeroInteractions(fail);

    promise.then(futureConsumer);
    verify(futureConsumer).accept(instance);
    verifyNoMoreInteractions(futureConsumer);
  }

  @Test public void promiseRejected() {
    final Throwable instance = new Throwable();
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    completableFuture.completeExceptionally(instance);
    final Promise<Object> promise = Promises.forCompletionStage(completableFuture);

    assertThat(promise.isDone(), is(true));
    assertThat(promise.isCancelled(), is(false));
    try {
      promise.claim();
    } catch (RuntimeException e) {
      assertSame(instance, e.getCause());
    }

    promise.then(compose(done, fail));
    verifyZeroInteractions(done);
    verify(fail).accept(instance);

    promise.then(futureConsumer);
    verify(futureConsumer).fail(instance);
    verifyNoMoreInteractions(futureConsumer);
  }

  @Test public void promiseOfCompletionStageSettingValue() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    final Promise<Object> promise = Promises.forCompletionStage(completableFuture);

    // register call backs
    promise.then(compose(done, fail));
    promise.then(futureConsumer);

    assertThat(promise.isDone(), is(false));
    assertThat(promise.isCancelled(), is(false));

    final Object instance = new Object();
    completableFuture.complete(instance);

    assertThat(promise.isDone(), is(true));
    assertThat(promise.isCancelled(), is(false));
    assertThat(promise.claim(), is(instance));

    verify(done).accept(instance);
    verifyZeroInteractions(fail);

    verify(futureConsumer).accept(instance);
    verifyNoMoreInteractions(futureConsumer);
  }

  @Test public void promiseOfCompletionStageSettingException() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    final Promise<Object> promise = Promises.forCompletionStage(completableFuture);

    // register call backs
    promise.then(compose(done, fail));
    promise.then(futureConsumer);

    assertThat(promise.isDone(), is(false));
    assertThat(promise.isCancelled(), is(false));

    final Throwable instance = new Throwable();
    completableFuture.completeExceptionally(instance);

    assertThat(promise.isDone(), is(true));
    assertThat(promise.isCancelled(), is(false));
    try {
      promise.claim();
    } catch (RuntimeException t) {
      assertSame(instance, t.getCause());
    }

    verifyZeroInteractions(done);
    verify(fail).accept(instance);

    verify(futureConsumer).fail(instance);
    verifyNoMoreInteractions(futureConsumer);
  }

  @Test public void mapPromiseSettingValue() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    final Promise<Object> originalPromise = Promises.forCompletionStage(completableFuture);
    final Promise<SomeObject> transformedPromise = originalPromise.map(SomeObject::new);

    assertThat(originalPromise.isDone(), is(false));
    assertThat(originalPromise.isCancelled(), is(false));

    assertThat(transformedPromise.isDone(), is(false));
    assertThat(transformedPromise.isCancelled(), is(false));

    final Object instance = new Object();
    completableFuture.complete(instance);
    assertThat(originalPromise.isDone(), is(true));
    assertThat(originalPromise.isCancelled(), is(false));

    assertThat(originalPromise.claim(), is(instance));

    assertThat(transformedPromise.isDone(), is(true));
    assertThat(transformedPromise.isCancelled(), is(false));

    @SuppressWarnings("unchecked")
    final Consumer<SomeObject> someObjectCallback = mock(Consumer.class);
    transformedPromise.then(compose(someObjectCallback, fail));

    assertThat(transformedPromise.claim().object, is(instance));
    verify(someObjectCallback).accept(argThat(new SomeObjectMatcher(instance)));
    verifyZeroInteractions(fail);
  }

  @Test public void mapPromiseSettingException() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    final Promise<Object> originalPromise = Promises.forCompletionStage(completableFuture);
    final Promise<SomeObject> transformedPromise = originalPromise.map(SomeObject::new);

    assertThat(originalPromise.isDone(), is(false));
    assertThat(originalPromise.isCancelled(), is(false));

    assertThat(transformedPromise.isDone(), is(false));
    assertThat(transformedPromise.isCancelled(), is(false));

    final Throwable instance = new Throwable();
    completableFuture.completeExceptionally(instance);
    assertThat(originalPromise.isDone(), is(true));
    assertThat(originalPromise.isCancelled(), is(false));

    try {
      originalPromise.claim();
    } catch (RuntimeException e) {
      assertSame(instance, e.getCause());
    }

    assertThat(transformedPromise.isDone(), is(true));
    assertThat(transformedPromise.isCancelled(), is(false));

    @SuppressWarnings("unchecked")
    final Consumer<SomeObject> someObjectCallback = mock(Consumer.class);
    transformedPromise.then(compose(someObjectCallback, fail));

    try {
      transformedPromise.claim();
    } catch (RuntimeException e) {
      assertSame(instance, e.getCause());
    }
    verifyZeroInteractions(someObjectCallback);
    verify(fail).accept(instance);
  }

  @Test(expected = IllegalStateException.class) public void flatMapFunctionThrowsException() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    final Promise<Object> originalPromise = Promises.forCompletionStage(completableFuture);
    final Promise<SomeObject> transformedPromise = originalPromise.flatMap(ignored -> {
      throw new IllegalStateException();
    });

    completableFuture.complete(new SomeObject("hi"));
    transformedPromise.claim();
  }

  @Test public void whenSequencePromiseSettingValue() {

    final CompletableFuture<Object> f1 = new CompletableFuture<>();
    final CompletableFuture<Object> f2 = new CompletableFuture<>();

    final Promise<Object> p1 = Promises.forCompletionStage(f1);
    final Promise<Object> p2 = Promises.forCompletionStage(f2);

    @SuppressWarnings("unchecked")
    final Promise<List<Object>> seq = Promises.when(p1, p2);
    @SuppressWarnings("unchecked")
    final Consumer<List<Object>> doneCallback = mock(Consumer.class);
    seq.then(compose(doneCallback, fail));

    assertThat(p1.isDone(), is(false));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(false));
    assertThat(seq.isCancelled(), is(false));

    final Object instance1 = new Object();
    f1.complete(instance1);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(false));
    assertThat(seq.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verifyZeroInteractions(fail);

    final Object instance2 = new Object();
    f2.complete(instance2);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(true));
    assertThat(seq.isCancelled(), is(false));

    assertThat(seq.claim(), contains(instance1, instance2));
    verify(doneCallback).accept(Arrays.asList(instance1, instance2));
    verifyZeroInteractions(fail);
  }

  @Test public void whenSequencePromiseSettingException() {

    final CompletableFuture<Object> f1 = new CompletableFuture<>();
    final CompletableFuture<Object> f2 = new CompletableFuture<>();

    final Promise<Object> p1 = Promises.forCompletionStage(f1);
    final Promise<Object> p2 = Promises.forCompletionStage(f2);

    @SuppressWarnings("unchecked")
    final Promise<List<Object>> sequenced = Promises.when(p1, p2);
    @SuppressWarnings("unchecked")
    final Consumer<List<Object>> doneCallback = mock(Consumer.class);
    sequenced.then(compose(doneCallback, fail));

    assertThat(p1.isDone(), is(false));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(sequenced.isDone(), is(false));
    assertThat(sequenced.isCancelled(), is(false));

    final Throwable throwable = new Throwable();
    f1.completeExceptionally(throwable);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(true));

    assertThat(sequenced.isDone(), is(true));
    assertThat(sequenced.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verify(fail).accept(throwable);

    final Throwable instance2 = new Throwable();
    f2.completeExceptionally(instance2);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(true));

    assertThat(sequenced.isDone(), is(true));
    assertThat(sequenced.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verifyNoMoreInteractions(fail);

    try {
      sequenced.claim();
    } catch (RuntimeException e) {
      assertSame(throwable, e.getCause());
    }
  }

  @Test public void newWhenSequencePromiseSettingValue() {

    final CompletableFuture<Object> f1 = new CompletableFuture<>();
    final CompletableFuture<Object> f2 = new CompletableFuture<>();

    final Promise<Object> p1 = Promises.forCompletionStage(f1);
    final Promise<Object> p2 = Promises.forCompletionStage(f2);

    @SuppressWarnings("unchecked")
    final Promise<List<Object>> seq = Promises.when(p1, p2);
    @SuppressWarnings("unchecked")
    final Consumer<List<Object>> doneCallback = mock(Consumer.class);
    seq.then(compose(doneCallback, fail));

    assertThat(p1.isDone(), is(false));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(false));
    assertThat(seq.isCancelled(), is(false));

    final Object instance1 = new Object();
    f1.complete(instance1);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(false));
    assertThat(seq.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verifyZeroInteractions(fail);

    final Object instance2 = new Object();
    f2.complete(instance2);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(false));

    assertThat(seq.isDone(), is(true));
    assertThat(seq.isCancelled(), is(false));

    assertThat(seq.claim(), contains(instance1, instance2));
    verify(doneCallback).accept(Arrays.asList(instance1, instance2));
    verifyZeroInteractions(fail);
  }

  @Test public void newWhenSequencePromiseSettingException() {

    final CompletableFuture<Object> f1 = new CompletableFuture<>();
    final CompletableFuture<Object> f2 = new CompletableFuture<>();

    final Promise<Object> p1 = Promises.forCompletionStage(f1);
    final Promise<Object> p2 = Promises.forCompletionStage(f2);

    @SuppressWarnings("unchecked")
    final Promise<List<Object>> sequenced = Promises.when(p1, p2);
    @SuppressWarnings("unchecked")
    final Consumer<List<Object>> doneCallback = mock(Consumer.class);
    sequenced.then(compose(doneCallback, fail));

    assertThat(p1.isDone(), is(false));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(false));
    assertThat(p2.isCancelled(), is(false));

    assertThat(sequenced.isDone(), is(false));
    assertThat(sequenced.isCancelled(), is(false));

    final Throwable throwable = new Throwable();
    f1.completeExceptionally(throwable);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(true));

    assertThat(sequenced.isDone(), is(true));
    assertThat(sequenced.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verify(fail).accept(throwable);

    final Throwable instance2 = new Throwable();
    f2.completeExceptionally(instance2);

    assertThat(p1.isDone(), is(true));
    assertThat(p1.isCancelled(), is(false));

    assertThat(p2.isDone(), is(true));
    assertThat(p2.isCancelled(), is(true));

    assertThat(sequenced.isDone(), is(true));
    assertThat(sequenced.isCancelled(), is(false));

    verifyZeroInteractions(doneCallback);
    verifyNoMoreInteractions(fail);

    try {
      sequenced.claim();
    } catch (RuntimeException e) {
      assertSame(throwable, e.getCause());
    }
  }

  @Test public void doneAddsFuturecompose() {
    final AtomicReference<String> ref = new AtomicReference<>();
    final CompletableFuture<String> f = new CompletableFuture<>();
    Promise<String> p = Promises.forCompletionStage(f);
    p.done(s -> ref.getAndSet("called: " + s));

    assertThat(ref.get(), is(nullValue()));
    f.complete("done!");
    assertThat(ref.get(), is("called: done!"));
  }

  @Test public void failAddsFuturecompose() {
    final AtomicReference<Throwable> ref = new AtomicReference<>();
    final CompletableFuture<String> f = new CompletableFuture<>();
    Promise<String> p = Promises.forCompletionStage(f);
    p.fail(ref::set);

    assertThat(ref.get(), is(nullValue()));
    Throwable ex = new RuntimeException("boo yaa!");
    f.completeExceptionally(ex);
    assertThat(ref.get(), is(ex));
  }

  @Test public void cancellationFrom() {
    final CompletableFuture<Object> completableFuture = new CompletableFuture<>();
    Promise<Object> promise = Promises.forCompletionStage(completableFuture);
    completableFuture.cancel(false);
    assertThat(promise.isCancelled(), is(true));
  }

  @Test public void cancellationTo() throws ExecutionException, InterruptedException {
    Future<Object> future = new FutureTask<>(() -> null);
    Promise<Object> promise = Promises.forFuture(future, java.util.concurrent.Executors.newSingleThreadExecutor());
    final CompletableFuture<Object> completableFuture = Promises.toCompletableFuture(promise);
    future.cancel(false);
    Thread.sleep(1000); // FutureTask works in a different thread so the
                        // propagation is not immediate.
    assertThat(promise.isCancelled(), is(true));
    assertThat(completableFuture.isCancelled(), is(true));
  }

  private static class SomeObject {
    public final Object object;

    private SomeObject(Object object) {
      this.object = object;
    }
  }

  private static final class SomeObjectMatcher extends BaseMatcher<SomeObject> {
    private final Object instance;

    public SomeObjectMatcher(Object instance) {
      this.instance = instance;
    }

    @Override public boolean matches(Object item) {
      return ((SomeObject) item).object.equals(instance);
    }

    @Override public void describeTo(Description description) {
      description.appendText("SomeObject matcher");
    }
  }
}
