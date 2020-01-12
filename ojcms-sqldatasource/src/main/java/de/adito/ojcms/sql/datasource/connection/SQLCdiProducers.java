package de.adito.ojcms.sql.datasource.connection;

import de.adito.ojcms.sql.datasource.util.*;
import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.*;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.*;
import java.sql.*;

/**
 * Defines CDI producers for {@link Connection} and {@link OJSQLBuilder}.
 *
 * @author Simon Danner, 29.12.2019
 */
@ApplicationScoped
final class SQLCdiProducers
{
  private SQLCdiProducers()
  {
  }

  /**
   * Produces a database connection supplier from a {@link IDatabaseConfig} for the application.
   *
   * @param pDatabaseConfig the config to resolve the connection supplier from
   * @return the database connection supplier for the application
   */
  @ApplicationScoped
  @Produces
  private static IDatabaseConnectionSupplier _produceConnectionSupplier(IDatabaseConfig pDatabaseConfig)
  {
    return pDatabaseConfig.createConnectionSupplier(new ConnectionSupplierFactory(false));
  }

  /**
   * Produces the {@link IDatabasePlatform} to use for the application.
   *
   * @param pConnectionSupplier the connection supplier to retrieve the platform from
   * @return the database platform to use for the application
   */
  @ApplicationScoped
  @Produces
  private static IDatabasePlatform _producePlatform(IDatabaseConnectionSupplier pConnectionSupplier)
  {
    return pConnectionSupplier.getPlatform();
  }

  /**
   * Produces the {@link Connection} for every single transaction.
   *
   * @param pConnectionSupplier the applications's connection supplier/creator
   * @return the connection for a transaction
   */
  @TransactionalScoped
  @Produces
  private static Connection _produceConnection(IDatabaseConnectionSupplier pConnectionSupplier)
  {
    return pConnectionSupplier.createNewConnection();
  }

  /**
   * Disposes the {@link Connection} after a transaction has been finished.
   *
   * @param pConnection the connection to dispose
   */
  private static void _disposeConnection(@Disposes Connection pConnection)
  {
    try
    {
      pConnection.close();
    }
    catch (SQLException pE)
    {
      throw new OJSQLException("Unable to close database connection", pE);
    }
  }

  /**
   * Produces a global {@link OJSQLBuilder} that works outside of transactions to initialize the database etc.
   * Use {@link GlobalBuilder} at injection points to receive this one.
   *
   * @param pConfig           the database config to use for the application
   * @param pDatabasePlatform the database platform to use for the application
   * @param pSerializer       the serializer for the SQL builder
   * @return the global SQL builder
   */
  @ApplicationScoped
  @GlobalBuilder
  @Produces
  private static OJSQLBuilder _produceSQLBuilderForInitialization(IDatabaseConfig pConfig, IDatabasePlatform pDatabasePlatform,
                                                                  BeanSQLSerializer pSerializer)
  {
    final IDatabaseConnectionSupplier connectionSupplier = pConfig.createConnectionSupplier(new ConnectionSupplierFactory(true));

    return OJSQLBuilderFactory.newSQLBuilder(pDatabasePlatform, pConfig.getDefaultIdColumnName())
        .withCustomSerializer(pSerializer)
        .withClosingAndRenewingConnection(connectionSupplier)
        .create();
  }

  /**
   * Produces a {@link OJSQLBuilder} with a permanent connection for every single transaction.
   *
   * @param pConfig           the database config to use for the application
   * @param pDatabasePlatform the database platform to use for the application
   * @param pConnection       the connection for the transaction
   * @param pSerializer       the serializer for the SQL builder
   * @return the transactional SQL builder
   */
  @TransactionalScoped
  @Produces
  private static OJSQLBuilder _produceTransactionSQLBuilder(IDatabaseConfig pConfig, IDatabasePlatform pDatabasePlatform,
                                                            Connection pConnection, BeanSQLSerializer pSerializer)
  {
    return OJSQLBuilderFactory.newSQLBuilder(pDatabasePlatform, pConfig.getDefaultIdColumnName())
        .withCustomSerializer(pSerializer)
        .withPermanentConnection(new IDatabaseConnectionSupplier()
        {
          @Override
          public Connection createNewConnection()
          {
            return pConnection;
          }

          @Override
          public IDatabasePlatform getPlatform()
          {
            return pDatabasePlatform;
          }
        })
        .create();
  }
}
