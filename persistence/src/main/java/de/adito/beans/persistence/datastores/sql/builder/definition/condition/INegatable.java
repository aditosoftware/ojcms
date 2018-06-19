package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

/**
 * Describes a negatable condition.
 *
 * @param <NEGATABLE> the concrete generic type of this negatable
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
