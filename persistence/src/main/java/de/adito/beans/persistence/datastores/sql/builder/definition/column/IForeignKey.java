package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The target of a foreign key relation within a database system.
 *
 * @author Simon Danner, 02.07.2018
 */
public interface IForeignKey extends IStatementFormat
{
  /**
   * The table name the foreign key refers to.
   *
   * @return a table name
   */
  String getTableName();

  /**
   * The column names the foreign key refers to (may be more for composite primary keys).
   *
   * @return a list of column names
   */
  List<String> getColumnNames();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType)
  {
    return getTableName() + "(" + getColumnNames().stream().collect(Collectors.joining(", ")) + ")";
  }

  /**
   * Creates a foreign key instance.
   *
   * @param pTableName  the table name the foreign key refers to
   * @param pColumnName the column name the foreign key refers to
   * @return a foreign key instance
   */
  static IForeignKey of(String pTableName, String pColumnName)
  {
    return of(pTableName, Collections.singleton(pColumnName));
  }

  /**
   * Creates a foreign key instance.
   *
   * @param pTableName   the table name the foreign key refers to
   * @param pColumnNames the column names the foreign key refers to (composite primary key of the other table)
   * @return a foreign key instance
   */
  static IForeignKey of(String pTableName, Collection<String> pColumnNames)
  {
    return new IForeignKey()
    {
      @Override
      public String getTableName()
      {
        return pTableName;
      }

      @Override
      public List<String> getColumnNames()
      {
        return new ArrayList<>(pColumnNames);
      }
    };
  }
}
