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

  public EmbeddedConnectionProvider(IEmbeddedDatabasePlatform pPlatform)
  {
    platform = pPlatform;
  }

  @Override
  public Connection createNewConnection()
  {
    try
    {
      return DriverManager.getConnection(platform.getConnectionString());
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
