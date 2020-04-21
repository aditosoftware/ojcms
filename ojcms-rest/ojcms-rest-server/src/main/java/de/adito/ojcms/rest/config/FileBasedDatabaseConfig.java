package de.adito.ojcms.rest.config;

import de.adito.ojcms.sql.datasource.connection.IDatabaseConfig;
import de.adito.ojcms.sqlbuilder.platform.*;
import de.adito.ojcms.sqlbuilder.platform.connection.*;
import de.adito.ojcms.utils.config.AbstractFileBasedConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Filed based implementation of {@link IDatabaseConfig}.
 * Expects a file in the execution environment.
 * The name for the file and all config keys are declared in this classes static constants.
 *
 * @author Simon Danner, 12.01.2020
 */
@ApplicationScoped
class FileBasedDatabaseConfig extends AbstractFileBasedConfig implements IDatabaseConfig
{
  private static final String CONFIG_PATH = "ojcms_database.properties";
  private static final String KEY_PLATFORM = "platform";
  private static final String KEY_EMBEDDED = "embedded";
  private static final String KEY_HOST = "host";
  private static final String KEY_PORT = "port";
  private static final String KEY_DB_NAME = "databaseName";
  private static final String KEY_USER = "user";
  private static final String KEY_PASSWORD = "password";

  private final _IConnectionSupplierResolver resolver;

  /**
   * Initializes the filed based config.
   * Loads all relevant properties from the config file.
   */
  FileBasedDatabaseConfig()
  {
    super(CONFIG_PATH);
    final String platform = _loadProperty(KEY_PLATFORM, properties, true).toUpperCase();
    final boolean embedded = _resolveEmbedded(properties);

    if (embedded)
    {
      final EEmbeddedDatabasePlatform embeddedDatabasePlatform = EEmbeddedDatabasePlatform.valueOf(platform);
      resolver = new _EmbeddedDatabaseResolver(embeddedDatabasePlatform);
    }
    else
    {
      final EExternalDatabasePlatform externalDatabasePlatform = EExternalDatabasePlatform.valueOf(platform);
      resolver = new _ExternalDatabaseResolver(externalDatabasePlatform, properties);
    }
  }

  @Override
  public IDatabaseConnectionSupplier createConnectionSupplier(ConnectionSupplierFactory pSupplierFactory)
  {
    return resolver.resolveConnectionSupplier(pSupplierFactory);
  }

  /**
   * Resolves the embedded flag from the properties file.
   * If there is no entry, the default 'false' will be used.
   *
   * @param pProperties a {@link Properties} instance to retrieve entries from the config file
   * @return <tt>true</tt> if an embedded database should be used
   */
  private static boolean _resolveEmbedded(Properties pProperties)
  {
    final String embedded = _loadProperty(KEY_EMBEDDED, pProperties, false);
    return Optional.ofNullable(embedded) //
        .map(pValue -> Boolean.parseBoolean(embedded)) //
        .orElse(false);
  }

  /**
   * Loads a property from the config file.
   * If the property is mandatory and the file does not contain an entry for the requested key, a runtime exception will be thrown.
   *
   * @param pKey        the key to resolve the config entry for
   * @param pProperties a {@link Properties} instance to retrieve entries from the config file
   * @param pMandatory  <tt>true</tt> if the config entry is mandatory
   * @return the resolved config value for the requested property
   */
  private static String _loadProperty(String pKey, Properties pProperties, boolean pMandatory)
  {
    if (pMandatory && !pProperties.containsKey(pKey))
      throw new DatabaseConfigException("Missing key " + pKey + " in database config file!");

    return pProperties.getProperty(pKey, null);
  }

  /**
   * Resolves the {@link IDatabaseConnectionSupplier} for external database systems configured in the config file.
   */
  private static class _ExternalDatabaseResolver implements _IConnectionSupplierResolver
  {
    private final EExternalDatabasePlatform platform;
    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;

    /**
     * Initializes the resolver with the platform and the properties instance to resolve all required information.
     *
     * @param pPlatform   the external database platform to use
     * @param pProperties a {@link Properties} instance to retrieve entries from the config file
     */
    _ExternalDatabaseResolver(EExternalDatabasePlatform pPlatform, Properties pProperties)
    {
      platform = pPlatform;
      host = _loadProperty(KEY_HOST, pProperties, true);
      port = _resolvePort(pProperties);
      databaseName = _loadProperty(KEY_DB_NAME, pProperties, true);
      username = _loadProperty(KEY_USER, pProperties, false);
      password = _loadProperty(KEY_PASSWORD, pProperties, false);
    }

    @Override
    public IDatabaseConnectionSupplier resolveConnectionSupplier(ConnectionSupplierFactory pFactory)
    {
      return pFactory.forExternalDatabase(platform, host, port, databaseName, username, password);
    }

    /**
     * Resolves the port from the properties file.
     *
     * @param pProperties a {@link Properties} instance to retrieve entries from the config file
     * @return the port to use for the database connection
     */
    private static int _resolvePort(Properties pProperties)
    {
      try
      {
        return Integer.parseInt(_loadProperty(KEY_PORT, pProperties, true));
      }
      catch (NumberFormatException pE)
      {
        throw new DatabaseConfigException("Bad number format for port!", pE);
      }
    }
  }

  /**
   * Resolves the {@link IDatabaseConnectionSupplier} for embedded database systems configured in the config file.
   */
  private static class _EmbeddedDatabaseResolver implements _IConnectionSupplierResolver
  {
    private final EEmbeddedDatabasePlatform platform;

    /**
     * Initializes the resolver.
     *
     * @param pPlatform the embedded database platform to use
     */
    _EmbeddedDatabaseResolver(EEmbeddedDatabasePlatform pPlatform)
    {
      platform = pPlatform;
    }

    @Override
    public IDatabaseConnectionSupplier resolveConnectionSupplier(ConnectionSupplierFactory pFactory)
    {
      return pFactory.forEmbeddedDatabase(platform, false);
    }
  }

  /**
   * A resolver for {@link IDatabaseConnectionSupplier}.
   */
  @FunctionalInterface
  private interface _IConnectionSupplierResolver
  {
    /**
     * Resolves the connections supplier by a {@link ConnectionSupplierFactory}.
     *
     * @param pFactory the factory to obtain platform based connection suppliers
     * @return the database connection supplier to use for the connections
     */
    IDatabaseConnectionSupplier resolveConnectionSupplier(ConnectionSupplierFactory pFactory);
  }
}
