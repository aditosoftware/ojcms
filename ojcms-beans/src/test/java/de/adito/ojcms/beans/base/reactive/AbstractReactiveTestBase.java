package de.adito.ojcms.beans.base.reactive;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base class for a reactive test case based on {@link Observable}.
 * Starting with a base instance that may be observable and the test is based on, a {@link TestObserver} will be registered at
 * the observer provided by the test case implementation.
 *
 * In a pipelining mechanism it is possible to define multiple assertions based on the observable.
 * Finally a terminating operation has to be provided that causes the observer to receive values.
 *
 * @param <BASE>     the type of the base instance the observable is based on
 * @param <OBSERVED> the type of the observed values
 * @param <TEST>     the runtime type of the reactive test
 * @author Simon Danner, 02.01.2019
 */
abstract class AbstractReactiveTestBase<BASE, OBSERVED, TEST extends AbstractReactiveTestBase<BASE, OBSERVED, TEST>>
{
  protected final BASE base;
  private final TestObserver<OBSERVED> testObserver = TestObserver.create();
  private final List<Consumer<TestObserver<OBSERVED>>> assertions = new ArrayList<>();

  /**
   * Creates the reactive test.
   *
   * @param pBase       the base instance the test is based on (may produce the observable)
   * @param pObservable the observable to make assertions on
   */
  protected AbstractReactiveTestBase(BASE pBase, Observable<OBSERVED> pObservable)
  {
    base = pBase;
    pObservable.subscribe(testObserver);
  }

  /**
   * Makes an assertion that the observer has been called certain times.
   *
   * @param pExpectedCallCount the expected amount of calls to the observer
   * @return the test instance itself to enable a pipelining mechanism
   */
  public TEST assertCallCount(int pExpectedCallCount)
  {
    return assertThat(pObserver -> pObserver.assertValueCount(pExpectedCallCount));
  }

  /**
   * Makes an assertions on every value received by the observer.
   *
   * @param pValueAssertion the assertion as consumer of one emitted value
   * @return the test instance itself to enable a pipelining mechanism
   */
  public TEST assertOnEveryValue(Consumer<OBSERVED> pValueAssertion)
  {
    return assertThat(pObserver -> pObserver.values().forEach(pValueAssertion));
  }

  /**
   * Makes an assertion based on the {@link TestObserver}.
   * It provides some predefined assertions provided by the RX framework.
   *
   * @param pAssertion the assertion as consumer of a {@link TestObserver}
   * @return the test instance itself to enable a pipelining mechanism
   */
  public TEST assertThat(Consumer<TestObserver<OBSERVED>> pAssertion)
  {
    assertions.add(pAssertion);
    //noinspection unchecked
    return (TEST) this;
  }

  /**
   * Includes the assertions from another reactive test.
   * This may be useful for multiple observers but one test case.
   *
   * @param pOtherTest the other reactive test to include the assertions from
   * @return the test instance itself to enable a pipelining mechanism
   */
  public TEST assertMultiple(AbstractReactiveTestBase<BASE, OBSERVED, TEST> pOtherTest)
  {
    assertions.addAll(pOtherTest.assertions);
    //noinspection unchecked
    return (TEST) this;
  }

  /**
   * Terminates the test definition and performs an action causing the emission of values.
   *
   * @param pAction the action to trigger the observable to emit values
   */
  protected void whenDoing(Consumer<BASE> pAction)
  {
    pAction.accept(base);
    assertions.forEach(pAssertion -> pAssertion.accept(testObserver));
  }
}
