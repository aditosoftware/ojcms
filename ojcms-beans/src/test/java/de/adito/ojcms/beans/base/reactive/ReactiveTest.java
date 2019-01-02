package de.adito.ojcms.beans.base.reactive;

import io.reactivex.Observable;

import java.util.function.*;

/**
 * A reactive test tool for {@link Observable}.
 * This class should be used if the test is based on an instance providing the observable to test.
 * The base instance may also be used to trigger the emission of values later on.
 *
 * @param <BASE>     the type of the base instance the observable is based on
 * @param <OBSERVED> the type of the observed values
 * @author Simon Danner, 02.01.2019
 */
public class ReactiveTest<BASE, OBSERVED> extends AbstractReactiveTest<BASE, OBSERVED, ReactiveTest<BASE, OBSERVED>>
{
  /**
   * Creates a new test definition starting with a base instance and a function to resolve the observable of the instance.
   * An {@link io.reactivex.observers.TestObserver} will be registered at the observable.
   *
   * Use methods of the base class the define assertions for the reactive test.
   *
   * @param pBase               the base instance the test is based on (may produce the observable)
   * @param pObservableResolver a function to resolve the observable from the base instance
   * @param <BASE>              the type of the base instance the observable is based on
   * @param <OBSERVED>          the type of the observed values
   * @return the reactive test definition
   */
  public static <BASE, OBSERVED> ReactiveTest<BASE, OBSERVED> observe(BASE pBase, Function<BASE, Observable<OBSERVED>> pObservableResolver)
  {
    return new ReactiveTest<>(pBase, pObservableResolver.apply(pBase));
  }

  /**
   * Creates the reactive test definition.
   *
   * @param pBase       the base instance the test is based on (may produce the observable)
   * @param pObservable the observable to make assertions on
   */
  private ReactiveTest(BASE pBase, Observable<OBSERVED> pObservable)
  {
    super(pBase, pObservable);
  }

  /**
   * Preconfigures the base instance for the test.
   *
   * @param pPreparation the configuration as consumer of the base instance
   * @return the test instance itself to enable a pipelining mechanism
   */
  public ReactiveTest<BASE, OBSERVED> prepare(Consumer<BASE> pPreparation)
  {
    pPreparation.accept(base);
    return this;
  }

  @Override
  public void whenDoing(Consumer<BASE> pAction)
  {
    super.whenDoing(pAction);
  }
}
