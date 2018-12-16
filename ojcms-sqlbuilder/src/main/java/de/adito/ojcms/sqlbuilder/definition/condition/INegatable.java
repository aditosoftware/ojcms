package de.adito.ojcms.sqlbuilder.definition.condition;

/**
 * A negatable condition.
 *
 * @param <NEGATABLE> the runtime type of this negatable
 * @author Simon Danner, 10.06.2018
 */
public interface INegatable<NEGATABLE extends INegatable<NEGATABLE>>
{
  /**
   * Negates the condition.
   *
   * @return the concrete negatable object itself for a pipelining mechanism
   */
  NEGATABLE not();

  /**
   * Determines, if this condition is negated.
   *
   * @return <tt>true</tt> if the condition is negated
   */
  boolean isNegated();
}
