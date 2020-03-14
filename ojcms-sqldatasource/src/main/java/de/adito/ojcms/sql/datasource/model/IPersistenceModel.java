package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

/**
 * Defines a generic persistence model that must be initializes in the database.
 *
 * @author Simon Danner, 01.01.2020
 */
public interface IPersistenceModel
{
  /**
   * Initializes the structures defined by this persistence model within the database.
   *
   * @param pBuilder a builder to execute SQL statements
   */
  void initModelInDatabase(OJSQLBuilder pBuilder);
}
