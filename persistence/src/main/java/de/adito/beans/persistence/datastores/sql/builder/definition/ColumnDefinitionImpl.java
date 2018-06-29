package de.adito.beans.persistence.datastores.sql.builder.definition;

import java.util.*;

/**
 * Implementation for a column definition.
 * This class also provides a static helper method to resolve column types from Java types.
 *
 * @author Simon Danner, 28.06.2018
 */
class ColumnDefinitionImpl implements IColumnDefinition
{
  private final String columnName;
  private final EColumnType columnType;
  private final int columnSize;
  private final Set<EColumnModifier> columnModifiers;

  /**
   * Create a new column definition.
   *
   * @param pColumnName      the name of the column
   * @param pColumnType      the type of the column
   * @param pColumnSize      the size of the column (-1 if not set)
   * @param pColumnModifiers a variable amount of column modifiers
   */
  ColumnDefinitionImpl(String pColumnName, EColumnType pColumnType, int pColumnSize, EColumnModifier... pColumnModifiers)
  {
    columnName = pColumnName;
    columnType = pColumnType;
    columnSize = pColumnSize;
    columnModifiers = new HashSet<>(Arrays.asList(pColumnModifiers));
  }

  @Override
  public String getColumnName()
  {
    return columnName;
  }

  @Override
  public EColumnType getColumnType()
  {
    return columnType;
  }

  @Override
  public int getColumnSize()
  {
    return columnSize;
  }

  @Override
  public Set<EColumnModifier> getModifiers()
  {
    return Collections.unmodifiableSet(columnModifiers);
  }
}
