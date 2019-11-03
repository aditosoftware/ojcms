package de.adito.ojcms.sqlbuilder.platform.derby;

import de.adito.ojcms.sqlbuilder.platform.IEmbeddedDatabasePlatform;

/**
 * Defines an embedded DERBY platform.
 *
 * @author Simon Danner, 02.11.2019
 */
public class DerbyEmbeddedPlatform extends AbstractDerbyPlatform implements IEmbeddedDatabasePlatform
{
  @Override
  public String getConnectionString()
  {
    return "jdbc:derby:" + EMBEDDED_DB_NAME + ";create=true";
  }

  @Override
  public String getDriverName()
  {
    return "org.apache.derby.jdbc.EmbeddedDriver";
  }

  @Override
  protected void beforeDriverInit()
  {
    final String userHome = System.getProperty("user.home");
    System.setProperty("derby.system.home", userHome + "/.ojcms/embedded_db");
  }
}
