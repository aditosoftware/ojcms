package de.adito.ojcms.sql.datasource;

import de.adito.ojcms.sql.datasource.connection.IDatabaseConfig;
import de.adito.ojcms.sqlbuilder.platform.EEmbeddedDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.*;

import javax.enterprise.context.ApplicationScoped;

/**
 * The database config for test scenarios.
 * Uses an in-memory derby database.
 *
 * @author Simon Danner, 04.01.2020
 */
@ApplicationScoped
public class TestDatabaseConfig implements IDatabaseConfig
{
  @Override
  public IDatabaseConnectionSupplier createConnectionSupplier(ConnectionSupplierFactory pSupplierFactory)
  {
    return pSupplierFactory.forEmbeddedDatabase(EEmbeddedDatabasePlatform.DERBY, true);
  }
}
