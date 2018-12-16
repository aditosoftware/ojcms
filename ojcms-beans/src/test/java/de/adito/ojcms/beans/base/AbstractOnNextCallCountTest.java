package de.adito.ojcms.beans.base;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Abstract base class for every test class, that has to check how often an 'onNext()' method
 * of an {@link io.reactivex.Observer} has been called.
 *
 * @author Simon Danner, 15.07.2018
 */
public abstract class AbstractOnNextCallCountTest
{
  private final Map<Consumer<?>, Integer> callCountMapping = new HashMap<>();

  @BeforeEach
  public void resetCallCount()
  {
    callCountMapping.clear();
  }

  @AfterEach
  public void checkCallCount()
  {
    callCountMapping.forEach((pOnNext, pExpectedCount) -> {
      try
      {
        verify(pOnNext, times(pExpectedCount)).accept(any());
      }
      catch (Exception pE)
      {
        throw new AssertionError();
      }
    });
  }

  /**
   * Verifies a certain call count of 'onNext'-calls emitted by an {@link Observable}.
   *
   * @param pObservable        the observable to check the call count of
   * @param pExpectedCallCount the expected number of 'onNext'-calls
   */
  protected <OBSERVED> void justCallCheck(Observable<OBSERVED> pObservable, int pExpectedCallCount)
  {
    observeWithCallCheck(pObservable, pExpectedCallCount, pValue -> {});
  }

  /**
   * Subscribes an 'onNext'-consumer to an {@link Observable} and registers an expected call count to verify after the unit test.
   *
   * @param pObservable        the observable to subscribe on
   * @param pExpectedCallCount the expected number of 'onNext'-calls
   * @param pOnNext            the consumer for the objects emitted by the observable to check to calls afterwards
   * @param <OBSERVED>         the type of the observed values
   */
  protected <OBSERVED> void observeWithCallCheck(Observable<OBSERVED> pObservable, int pExpectedCallCount, Consumer<? super OBSERVED> pOnNext)
  {
    final Consumer<? super OBSERVED> spiedOnNext = spy(new _OnNextConsumer<>(pOnNext));
    pObservable.subscribe(spiedOnNext);
    callCountMapping.put(spiedOnNext, pExpectedCallCount);
  }

  /**
   * Wraps an actual {@link Consumer} to spy on it.
   *
   * @param <T> the type of the consumed data
   */
  private static class _OnNextConsumer<T> implements Consumer<T>
  {
    private final Consumer<T> originalConsumer;

    private _OnNextConsumer(Consumer<T> pOriginalConsumer)
    {
      originalConsumer = pOriginalConsumer;
    }

    @Override
    public void accept(T pArg) throws Exception
    {
      originalConsumer.accept(pArg);
    }
  }
}
