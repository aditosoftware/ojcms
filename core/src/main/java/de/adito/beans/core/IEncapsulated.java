package de.adito.beans.core;

import de.adito.beans.core.annotations.internal.Encapsulated;
import de.adito.beans.core.reactive.*;

import java.util.stream.*;

/**
 * The encapsulated data core of a bean element.
 * Defines the private scope on a Java level related to the bean data.
 *
 * Important: This interface must be package protected to enable data encapsulation.
 *
 * @param <CORE> the type of the elements in the core
 * @author Simon Danner, 25.01.2017
 */
@Encapsulated
interface IEncapsulated<CORE> extends Iterable<CORE>, ILinkable, IReferable, IEventReceiver, IEventPublisher
{
  /**
   * A stream of all core elements of this data core.
   *
   * @return a stream of the core elements
   */
  default Stream<CORE> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * A parallel stream of all core elements of this data core.
   *
   * @return a parallel stream of the core elements
   */
  default Stream<CORE> parallelStream()
  {
    return StreamSupport.stream(spliterator(), true);
  }
}
