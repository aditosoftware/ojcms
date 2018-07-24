package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.util.DBConnectionInfo;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * The target of a foreign key relation within a database system.
 *
 * @author Simon Danner, 02.07.2018
 */
public interface IForeignKey
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

  /**
   * Creates the referenced table, if necessary.
   * This mechanism may be used, if it cannot be guaranteed in which order tables must be created.
   *
   * @param pConnectionInfo connection information for the database
   */
  default void createReferencedTable(DBConnectionInfo pConnectionInfo)
  {
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
    return of(pTableName, pColumnName, null);
  }

  /**
   * Creates a foreign key instance.
   *
   * @param pTableName              the table name the foreign key refers to
   * @param pColumnName             the column name the foreign key refers to
   * @param pReferencedTableCreator an optional creator for the referenced table
   * @return a foreign key instance
   */
  static IForeignKey of(String pTableName, String pColumnName, @Nullable Consumer<DBConnectionInfo> pReferencedTableCreator)
  {
    return of(pTableName, Collections.singleton(pColumnName), pReferencedTableCreator);
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
    return of(pTableName, pColumnNames, null);
  }

  /**
   * Creates a foreign key instance.
   *
   * @param pTableName              the table name the foreign key refers to
   * @param pColumnNames            the column names the foreign key refers to (composite primary key of the other table)
   * @param pReferencedTableCreator an optional creator for the referenced table
   * @return a foreign key instance
   */
  static IForeignKey of(String pTableName, Collection<String> pColumnNames, @Nullable Consumer<DBConnectionInfo> pReferencedTableCreator)
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

      @Override
      public void createReferencedTable(DBConnectionInfo pConnectionInfo)
      {
        if (pReferencedTableCreator != null)
          pReferencedTableCreator.accept(pConnectionInfo);
      }
    };
  }
}
