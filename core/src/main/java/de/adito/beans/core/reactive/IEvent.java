package de.adito.beans.core.reactive;

/**
 * An event based on a source that triggered the event.
 *
 * @param <SOURCE> the type of the source the event is based on
 * @author Simon Danner, 18.11.2018
 */
public interface IEvent<SOURCE>
{
  /**
   * The source that triggered this event.
   *
   * @return the source of this event
   */
  SOURCE getSource();
}