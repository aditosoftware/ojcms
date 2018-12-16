package de.adito.ojcms.persistence.datastores;

import de.adito.ojcms.persistence.*;
import de.adito.ojcms.persistence.datastores.sql.*;
import de.adito.ojcms.sqlbuilder.definition.EDatabaseType;
import de.adito.ojcms.sqlbuilder.util.DBConnectionInfo;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/**
 * A factory for all available {@link IPersistentSourcesStore} implementations of the framework.
 * This is an entry point for the framework user to configure the persistence functionality.
 * The factory should mainly be used with {@link OJPersistence#configure(Function)}.
 *
 * @author Simon Danner, 26.02.2018
 */
public final class PersistentDataSourcesFactory
{
  private final Supplier<BeanDataStore> dataStoreSupplier;

  public PersistentDataSourcesFactory(Supplier<BeanDataStore> pDataStoreSupplier)
  {
    dataStoreSupplier = pDataStoreSupplier;
  }

  /**
   * Creates a SQL persistent bean data sources store.
   * This method uses no username and password for the database connection.
   *
   * @param pDatabaseType the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @return the persistent data store
   */
  public IPersistentSourcesStore sql(EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName)
  {
    return sql(pDatabaseType, pHost, pPort, pDatabaseName, null, null);
  }

  /**
   * Creates a SQL persistent bean data sources store.
   *
   * @param pDatabaseType the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @param pUserName     an optional user name for the connection
   * @param pPassword     an optional password for the connection
   * @return the persistent data store
   */
  public IPersistentSourcesStore sql(EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName,
                                     @Nullable String pUserName, @Nullable String pPassword)
  {
    final DBConnectionInfo dbConnectionInfo = new DBConnectionInfo(pDatabaseType, pHost, pPort, pDatabaseName, pUserName, pPassword);
    //noinspection unchecked
    return new CachingPersistentDataSources((pID, pBeanType) -> new SQLPersistentBean(pID, pBeanType, dbConnectionInfo, dataStoreSupplier),
                                            pId -> SQLPersistentBean.isDataSourceExisting(dbConnectionInfo, pId),
                                            (pId, pBeanType) -> new SQLPersistentContainer(pBeanType, dbConnectionInfo, pId, dataStoreSupplier),
                                            pExistingIds -> SQLPersistentBean.removeObsoletes(dbConnectionInfo, pExistingIds),
                                            pExistingIds -> SQLPersistentContainer.removeObsoletes(dbConnectionInfo, pExistingIds));
  }
}
