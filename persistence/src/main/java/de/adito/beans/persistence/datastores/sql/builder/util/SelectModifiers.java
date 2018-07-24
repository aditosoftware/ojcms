package de.adito.beans.persistence.datastores.sql.builder.util;

import de.adito.beans.persistence.datastores.sql.builder.definition.condition.WhereModifiers;

/**
 * The modifiers for a select statement.
 * It provides methods to build query strings based on the different modifiers.
 *
 * @author Simon Danner, 26.04.2018
 */
public class SelectModifiers extends WhereModifiers
{
  private boolean distinct = false;
  private boolean count = false;

  /**
   * Sets a count flag for the SQL statement.
   *
   * @param pCount <tt>true</tt>, if rows should be counted by the future statement
   */
  public void setCount(boolean pCount)
  {
    count = pCount;
  }

  /**
   * Sets a distinct flag for the SQL statement.
   *
   * @param pDistinct <tt>true</tt>, if only distinct values should be selected by the future query
   */
  public void setDistinct(boolean pDistinct)
  {
    distinct = pDistinct;
  }

  /**
   * Determines, if the select statement should return distinct values.
   *
   * @return <tt>true</tt>, if the values should be distinct
   */
  public boolean distinct()
  {
    return distinct;
  }

  /**
   * Determines, if the select statement should count rows.
   *
   * @return <tt>true</tt>, if the result should be counted
   */
  public boolean count()
  {
    return count;
  }
}
