package de.adito.beans.persistence.datastores.sql.builder.modifiers;

import de.adito.beans.persistence.datastores.sql.builder.util.IColumnIdentification;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The modifiers for a select statement.
 * It provides methods to build query strings based on the different modifiers.
 *
 * @author Simon Danner, 26.04.2018
 */
public class SelectModifiers extends WhereModifiers
{
  public static final String COUNT_RESULT = "rowNumber";
  private boolean distinct = false;
  private boolean count = false;

  public SelectModifiers(String pIdColumnName)
  {
    super(pIdColumnName);
  }

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
   * The query string based on the distinct flag.
   */
  public String distinct()
  {
    return distinct ? "DISTINCT " : "";
  }

  /**
   * The column query string for the select statement.
   *
   * @param pColumns the columns to select
   * @return the query string for the select statement
   */
  public String columns(List<IColumnIdentification<?>> pColumns)
  {
    String columns = pColumns.isEmpty() ? "*" :
        pColumns.stream()
            .map(pColumn -> pColumn.getColumnName().toUpperCase())
            .collect(Collectors.joining(", "));
    return count ? "COUNT(" + columns + ") AS " + COUNT_RESULT : columns;
  }
}
