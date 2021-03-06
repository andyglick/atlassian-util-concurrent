package io.atlassian.util.concurrent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.atlassian.util.concurrent.CompletionStages.unsafeBlockAndGet;
import static io.atlassian.util.concurrent.Timeout.getMillisTimeout;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class CompletionStagesTest {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test public void failSetsCompletionStageToCompletedExceptionally() throws ExecutionException, InterruptedException {
    final RuntimeException exception = new RuntimeException();
    CompletionStage<Object> completionStage = CompletionStages.fail(exception);

    assertThat(completionStage.toCompletableFuture().isCompletedExceptionally(), is(true));
    try {
      completionStage.toCompletableFuture().get();
    } catch (ExecutionException e) {
      assertThat(e.getCause(), equalTo(exception));
      return;
    }
    fail("correct exception not returned");
  }

  @Test public void getCompletionStageValueReturnsValue() {
    final String value = "value";
    CompletionStage<String> completionStage = CompletableFuture.completedFuture(value);

    assertThat(unsafeBlockAndGet(completionStage, throwable -> "no"), equalTo(value));
  }

  @Test public void getCompletionStageValueInvokesErrorFunction() {
    final RuntimeException exception = new RuntimeException();
    CompletionStage<String> completionStage = CompletionStages.fail(exception);
    String value = "value";

    assertThat(unsafeBlockAndGet(completionStage, throwable -> value), equalTo(value));
  }

  @Test public void getCompletionStageValueWithTimeoutReturnsValue() {
    final String value = "value";
    CompletionStage<String> completionStage = CompletableFuture.completedFuture(value);

    assertThat(unsafeBlockAndGet(completionStage, getMillisTimeout(10, TimeUnit.MILLISECONDS), throwable -> "no"), equalTo(value));
  }

  @Test public void getCompletionStageValueWithTimeoutInvokesErrorFunction() {
    final RuntimeException exception = new RuntimeException();
    CompletionStage<String> completionStage = CompletionStages.fail(exception);
    String value = "value";

    assertThat(unsafeBlockAndGet(completionStage, getMillisTimeout(10, TimeUnit.MILLISECONDS), throwable -> value), equalTo(value));
  }
}
