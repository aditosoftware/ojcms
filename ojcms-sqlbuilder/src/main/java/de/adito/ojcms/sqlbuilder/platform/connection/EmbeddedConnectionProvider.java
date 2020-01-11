package de.adito.ojcms.sqlbuilder.platform.connection;

import de.adito.ojcms.sqlbuilder.platform.*;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.sql.*;

/**
 * Implementation of {@link IDatabaseConnectionSupplier} for embedded databases.
 * It only requires the requested {@link IEmbeddedDatabasePlatform} because host address, port, etc. are all handled internally.
 *
 * @author Simon Danner, 02.11.2019
 */
public final class EmbeddedConnectionProvider implements IDatabaseConnectionSupplier
{
  private final IEmbeddedDatabasePlatform platform;
  private final boolean inMemory;
  private final boolean autoCommit;

  public EmbeddedConnectionProvider(IEmbeddedDatabasePlatform pPlatform, boolean pInMemory, boolean pAutoCommit)
  {
    platform = pPlatform;
    inMemory = pInMemory;
    autoCommit = pAutoCommit;
  }

  @Override
  public Connection createNewConnection()
  {
    try
    {
      final Connection connection = DriverManager.getConnection(platform.getConnectionString(inMemory));
      connection.setAutoCommit(autoCommit);
      return connection;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE, "Unable to connect to embedded database! platform = " + platform.getPlatformName());
    }
  }

  @Override
  public IDatabasePlatform getPlatform()
  {
    return platform;
  }
}
