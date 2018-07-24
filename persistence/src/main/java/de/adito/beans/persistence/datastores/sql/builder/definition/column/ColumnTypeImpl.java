package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Basic implementation for a database column type.
 *
 * @author Simon Danner, 02.07.2018
 */
class ColumnTypeImpl implements IColumnType
{
  private final EColumnType columnType;
  private int length = -1, precision = -1, scale = -1;
  private boolean isPrimaryKey;
  private IForeignKey foreignKey;
  private List<EColumnModifier> modifiers;

  ColumnTypeImpl(EColumnType pColumnType)
  {
    columnType = pColumnType;
  }

  @Override
  public EColumnType getType()
  {
    return columnType;
  }

  @Override
  public int getLength()
  {
    return length;
  }

  @Override
  public boolean hasLength()
  {
    return length >= 0;
  }

  @Override
  public IColumnType length(int pLength)
  {
    length = pLength;
    return this;
  }

  @Override
  public int getPrecision()
  {
    return precision;
  }

  @Override
  public boolean hasPrecision()
  {
    return precision >= 0;
  }

  @Override
  public IColumnType precision(int pPrecision)
  {
    precision = pPrecision;
    return this;
  }

  @Override
  public int getScale()
  {
    return scale;
  }

  @Override
  public boolean hasScale()
  {
    return scale >= 0;
  }

  @Override
  public IColumnType scale(int pScale)
  {
    scale = pScale;
    return this;
  }

  @Override
  public boolean isPrimaryKey()
  {
    return isPrimaryKey;
  }

  @Override
  public IColumnType primaryKey()
  {
    isPrimaryKey = true;
    return this;
  }

  @Override
  public @Nullable IForeignKey getForeignKey()
  {
    return foreignKey;
  }

  @Override
  public IColumnType foreignKey(IForeignKey pForeignKey)
  {
    foreignKey = pForeignKey;
    return this;
  }

  @Override
  public IColumnType modifiers(EColumnModifier... pModifiers)
  {
    modifiers = Arrays.asList(pModifiers.clone());
    return this;
  }

  @NotNull
  @Override
  public Iterator<EColumnModifier> iterator()
  {
    return modifiers == null ? Collections.emptyIterator() : modifiers.iterator();
  }
}
