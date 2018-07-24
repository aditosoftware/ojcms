package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import de.adito.beans.persistence.datastores.sql.builder.format.IStatementFormat;
import org.jetbrains.annotations.Nullable;

import java.util.stream.*;

/**
 * A SQL column type instance for a certain table.
 * Such a type is based on a {@link EColumnType}, which defines all possible base column types.
 * An instance can be configured through a pipelining mechanism implemented by the methods of this interface.
 *
 * @author Simon Danner, 02.07.2018
 */
public interface IColumnType extends IStatementFormat, Iterable<EColumnModifier>
{
  /**
   * The base column type this type instance is based on.
   *
   * @return a base column type
   */
  EColumnType getType();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    final String modifiers = streamModifiers()
        .map(pModifier -> pModifier.toStatementFormat(pDatabaseType, pIdColumnName))
        .collect(Collectors.joining(" "));
    return pDatabaseType.columnTypeToStatementFormat(this) + (modifiers.isEmpty() ? "" : " " + modifiers);
  }

  /**
   * The length of this column.
   * Might not be used by some base column types.
   *
   * @return a length for the column, or -1 if not present
   */
  default int getLength()
  {
    return -1;
  }

  /**
   * Determines, if this column type instance has a length information.
   *
   * @return <tt>true</tt>, if the length information is present
   */
  default boolean hasLength()
  {
    return false;
  }

  /**
   * Sets a length for the column type instance.
   *
   * @param pLength the length of the column
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType length(int pLength);

  /**
   * The precision of this column.
   * Might not be used by some base column types.
   *
   * @return a precision for the column, or -1 if not present
   */
  default int getPrecision()
  {
    return -1;
  }

  /**
   * Determines, if this column type instance has a precision information.
   *
   * @return <tt>true</tt>, if the precision information is present
   */
  default boolean hasPrecision()
  {
    return false;
  }

  /**
   * Sets a precision for the column type instance.
   *
   * @param pPrecision the precision of the column
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType precision(int pPrecision);

  /**
   * The scale of this column.
   * Might not be used by some base column types.
   *
   * @return a scale for the column, or -1 if not present
   */
  default int getScale()
  {
    return -1;
  }

  /**
   * Determines, if this column type instance has a scale information.
   *
   * @return <tt>true</tt>, if the scale information is present
   */
  default boolean hasScale()
  {
    return false;
  }

  /**
   * Sets a scale for the column type instance.
   *
   * @param pScale the scale of the column
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType scale(int pScale);

  /**
   * Determines, if this column type instance should be a primary key.
   *
   * @return <tt>true</tt>, if it should be a primary key
   */
  default boolean isPrimaryKey()
  {
    return false;
  }

  /**
   * Determines the column type instance as primary key.
   *
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType primaryKey();

  /**
   * The foreign key of this column type instance.
   * A {@link IForeignKey} is the target of a relation.
   *
   * @return a foreign key or null, if not present
   */
  @Nullable
  default IForeignKey getForeignKey()
  {
    return null;
  }

  /**
   * Sets the foreign key for the column type instance.
   * A {@link IForeignKey} is the target of a relation.
   *
   * @param pForeignKey the foreign key
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType foreignKey(IForeignKey pForeignKey);

  /**
   * Sets a variable amount of column modifiers for this column type instance.
   *
   * @param pModifiers the modifiers for the column
   * @return the type instance itself to enable a pipelining mechanism
   */
  IColumnType modifiers(EColumnModifier... pModifiers);

  /**
   * Determines, if this column type instance has a certain column modifier.
   *
   * @param pModifier the modifier to check
   * @return <tt>true</tt>, if the certain modifier is present
   */
  default boolean hasModifier(EColumnModifier pModifier)
  {
    return streamModifiers().anyMatch(pMod -> pMod == pModifier);
  }

  /**
   * A stream of all column modifiers of this column type instance.
   *
   * @return a stream of modifiers
   */
  default Stream<EColumnModifier> streamModifiers()
  {
    return StreamSupport.stream(spliterator(), false);
  }
}
