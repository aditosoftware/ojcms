package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.transactions.api.*;

/**
 * Defines a persistence model that is based on a {@link IBeanKey} to identify the actual data of the model.
 *
 * @param <KEY> the type of the key of this model
 * @author Simon Danner, 01.01.2020
 */
public interface IPersistenceModel<KEY extends IBeanKey>
{
  /**
   * Initializes the structures defined by this persistence model within the database.
   *
   * @param pBuilder a builder to execute SQL statements
   */
  void initModelInDatabase(OJSQLBuilder pBuilder);

  /**
   * Loads {@link PersistentBeanData} by the instance of a identifying key.
   *
   * @param pKey     the key to identify the bean data to load
   * @param pBuilder a builder to execute SQL statements
   * @return the loaded persistent bean data
   */
  PersistentBeanData loadDataByKey(KEY pKey, OJSQLBuilder pBuilder);
}
