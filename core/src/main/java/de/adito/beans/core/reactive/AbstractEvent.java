package de.adito.beans.core.reactive;

/**
 * Abstract implementation of {@link IEvent} to handle the source.
 *
 * @param <SOURCE> the type of the resource that triggered this event
 * @author Simon Danner, 25.11.2018
 */
public abstract class AbstractEvent<SOURCE> implements IEvent<SOURCE>
{
  private final SOURCE source;

  /**
   * Creates a new event.
   *
   * @param pSource the source that triggered the event
   */
  protected AbstractEvent(SOURCE pSource)
  {
    source = pSource;
  }

  @Override
  public SOURCE getSource()
  {
    return source;
  }
}
