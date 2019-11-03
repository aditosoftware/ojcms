package de.adito.ojcms.sqlbuilder.platform.derby;

import de.adito.ojcms.sqlbuilder.platform.IExternalDatabasePlatform;

/**
 * Defines an external DERBY platform.
 *
 * @author Simon Danner, 02.11.2019
 */
public class DerbyPlatform extends AbstractDerbyPlatform implements IExternalDatabasePlatform
{
  @Override
  public String getConnectionString(String pHost, int pPort, String pDatabaseName)
  {
    return "jdbc:derby://" + pHost + ":" + pPort + "/" + pDatabaseName + ";create=true";
  }

  @Override
  public String getDriverName()
  {
    return "org.apache.derby.jdbc.ClientDriver";
  }
}
