package de.adito.beans.core;

import de.adito.beans.core.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.beans.core.reactive.AbstractEvent;

/**
 * An event based on a source that holds its data in an {@link IEncapsulated} data core.
 * The triggered event can be published trough the data core because it is an {@link de.adito.beans.core.reactive.IEventReceiver}.
 *
 * @param <SOURCE> the type of the source that triggered the event (based on an {@link IEncapsulatedHolder} to provide a data core)
 * @author Simon Danner, 16.11.2018
 */
@RequiresEncapsulatedAccess
public abstract class AbstractChangeEvent<SOURCE extends IEncapsulatedHolder, EVENT extends AbstractChangeEvent<SOURCE, EVENT>>
    extends AbstractEvent<SOURCE>
{
  protected AbstractChangeEvent(SOURCE pSource)
  {
    super(pSource);
  }

  /**
   * Publishes this event to its source.
   */
  @SuppressWarnings("unchecked")
  void publishEventToSource()
  {
    final Class<EVENT> eventType = (Class<EVENT>) getClass();
    getSource().getEncapsulated().getEventObserverFromType(eventType).onNext((EVENT) this);
  }
}
