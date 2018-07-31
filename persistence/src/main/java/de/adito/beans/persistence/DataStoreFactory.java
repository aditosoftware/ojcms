package de.adito.beans.persistence;

import de.adito.beans.persistence.datastores.CachingBeanDataStore;
import de.adito.beans.persistence.datastores.sql.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import de.adito.beans.persistence.datastores.sql.builder.util.DBConnectionInfo;
import de.adito.beans.persistence.spi.IPersistentBeanDataStore;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A factory for all available {@link IPersistentBeanDataStore} implementations of the framework.
 * This is an entry point for the framework user to configure the persistence functionality.
 * The factory should mainly be used with {@link de.adito.beans.persistence.OJPersistence#configure(Function)}.
 *
 * @author Simon Danner, 26.02.2018
 */
public final class DataStoreFactory
{
  /**
   * Creates a SQL persistent bean data store.
   *
   * @param pDatabaseType the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @return the persistent data store
   */
  public IPersistentBeanDataStore sql(EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName)
  {
    return sql(pDatabaseType, pHost, pPort, pDatabaseName, null, null);
  }

  /**
   * Creates a SQL persistent bean data store.
   *
   * @param pDatabaseType the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @param pUserName     an optional user name for the connection
   * @param pPassword     an optional password for the connection
   * @return the persistent data store
   */
  public IPersistentBeanDataStore sql(EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName,
                                      @Nullable String pUserName, @Nullable String pPassword)
  {
    final DBConnectionInfo dbConnectionInfo = new DBConnectionInfo(pDatabaseType, pHost, pPort, pDatabaseName, pUserName, pPassword);
    //noinspection unchecked
    return new CachingBeanDataStore((pBeanId, pBeanType) -> new SQLPersistentBean(pBeanId, pBeanType, dbConnectionInfo, OJPersistence.dataStore()),
                                    (pContainerId, pBeanType) -> new SQLPersistentContainer(pBeanType, dbConnectionInfo, pContainerId, OJPersistence.dataStore()),
                                    pExistingSingleBeanIds -> SQLPersistentBean.removeObsoletes(dbConnectionInfo, pExistingSingleBeanIds),
                                    pExistingContainerIds -> SQLPersistentContainer.removeObsoletes(dbConnectionInfo, pExistingContainerIds));
  }
}
