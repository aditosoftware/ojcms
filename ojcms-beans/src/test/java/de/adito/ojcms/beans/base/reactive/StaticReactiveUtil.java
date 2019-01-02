package de.adito.ojcms.beans.base.reactive;

import io.reactivex.Observable;

/**
 * A reactive test tool for {@link Observable}.
 * This class should be used if the observable to test is created via a static method.
 *
 * @param <OBSERVED> the type of the observed values
 * @author Simon Danner, 02.01.2019
 */
public class StaticReactiveUtil<OBSERVED> extends AbstractReactiveTestBase<Void, OBSERVED, StaticReactiveUtil<OBSERVED>>
{
  /**
   * Creates a new test definition based on an {@link Observable}.
   * An {@link io.reactivex.observers.TestObserver} will be registered at the observable.
   *
   * Use methods of the base class the define assertions for the reactive test.
   *
   * @param pObservable the observable to test
   * @param <OBSERVED>  the type of the observed values
   * @return the reactive test definition
   */
  public static <OBSERVED> StaticReactiveUtil<OBSERVED> observe(Observable<OBSERVED> pObservable)
  {
    return new StaticReactiveUtil<>(pObservable);
  }

  /**
   * Creates the reactive test.
   *
   * @param pObservable the observable to make assertions on
   */
  private StaticReactiveUtil(Observable<OBSERVED> pObservable)
  {
    super(null, pObservable);
  }

  /**
   * Terminates the test definition and performs an action causing the emission of values.
   *
   * @param pAction the action to trigger the observable to emit values
   */
  public void whenDoing(Runnable pAction)
  {
    whenDoing(pBase -> pAction.run());
  }
}
