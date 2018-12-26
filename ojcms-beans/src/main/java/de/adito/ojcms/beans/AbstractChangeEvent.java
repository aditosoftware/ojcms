package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.reactive.AbstractEvent;

import static de.adito.ojcms.beans.BeanInternalEvents.requestEncapsulatedData;

/**
 * An event based on a source that holds its data in an {@link IEncapsulatedData} data core.
 * The triggered event can be published trough the data core because it is an {@link IEventReceiver}.
 *
 * @param <SOURCE> the type of the source that triggered the event (based on an {@link IEncapsulatedDataHolder} to provide a data core)
 * @author Simon Danner, 16.11.2018
 */
@RequiresEncapsulatedAccess
public abstract class AbstractChangeEvent<SOURCE extends IEncapsulatedDataHolder, EVENT extends AbstractChangeEvent<SOURCE, EVENT>>
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
    requestEncapsulatedData(getSource()).getEventObserverFromType(eventType).onNext((EVENT) this);
  }
}
