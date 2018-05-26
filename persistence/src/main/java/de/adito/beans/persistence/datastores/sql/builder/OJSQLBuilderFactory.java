package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.sql.*;
import java.util.function.Supplier;

/**
 * Factory for the SQL statement builders {@link OJSQLBuilder} and {@link OJSQLBuilderForTable}.
 * This is the only public entry point for the sql builder framework.
 * The builders can here be configured in a functional way.
 *
 * @author Simon Danner, 15.05.2018
 */
public final class OJSQLBuilderFactory
{
  private OJSQLBuilderFactory()
  {
  }

  /**
   * Creates a new builder to configure a SQL statement builder.
   * The default {@link Builder} is used to create an {@link OJSQLBuilder}.
   * If a sql builder for a single database table is necessary,
   * {@link Builder#forSingleTable(String)} can be used to create an {@link OJSQLBuilderForTable}
   *
   * @param pDatabaseType       the type of the database to use for the builder
   * @param pGlobalIdColumnName a global name for all id columns for this builder
   * @return a builder to configure the SQL statement builder
   */
  public static Builder newSQLBuilder(EDatabaseType pDatabaseType, String pGlobalIdColumnName)
  {
    return new Builder(pDatabaseType, pGlobalIdColumnName);
  }

  /**
   * A builder for the SQL statement builder to create a {@link OJSQLBuilder}.
   */
  public static class Builder extends _AbstractBuilder<OJSQLBuilder, Builder>
  {
    /**
     * Creates a new builder with the required information.
     *
     * @param pDatabaseType the type of the database to use for the builder
     * @param pIdColumnName a global name for all id columns for this builder
     */
    private Builder(EDatabaseType pDatabaseType, String pIdColumnName)
    {
      super(pDatabaseType, pIdColumnName);
    }

    /**
     * Creates a copy of a builder based an a {@link BuilderForTable}.
     * This constructor may be used to change from a single table builder to a normal one.
     *
     * @param pOther the other builder to create the copy from
     */
    private Builder(BuilderForTable pOther)
    {
      super(pOther);
    }

    /**
     * Configures the SQL builder for a single database table.
     *
     * @param pTableName the name of the database table
     * @return the builder for a single table
     */
    public BuilderForTable forSingleTable(String pTableName)
    {
      return new BuilderForTable(this, pTableName);
    }

    @Override
    public OJSQLBuilder create()
    {
      return new OJSQLBuilder(databaseType, connectionSupplier, closeConnectionAfterStatement, idColumnName);
    }
  }

  /**
   * A builder for the SQL statement builder to create a {@link OJSQLBuilderForTable}.
   * The later SQL builder is used for one database table only.
   */
  public static class BuilderForTable extends _AbstractBuilder<OJSQLBuilderForTable, BuilderForTable>
  {
    private final String tableName;

    /**
     * Creates a new builder with the required information.
     *
     * @param pDatabaseType the type of the database to use for the builder
     * @param pIdColumnName a global name for all id columns for the builder
     * @param pTableName    the name of the single table for the builder
     */
    private BuilderForTable(EDatabaseType pDatabaseType, String pIdColumnName, String pTableName)
    {
      super(pDatabaseType, pIdColumnName);
      tableName = pTableName;
    }

    /**
     * Creates a copy of a builder based an a {@link Builder}.
     * This constructor may be used to change from a normal builder to a single table builder.
     *
     * @param pOther     the other builder to create the copy from
     * @param pTableName the name of the single table for the builder
     */
    private BuilderForTable(Builder pOther, String pTableName)
    {
      super(pOther);
      tableName = pTableName;
    }

    /**
     * Configures the SQL builder for a multiple database tables.
     *
     * @return the builder the SQL statement builder
     */
    public Builder forMultipleTables()
    {
      return new Builder(this);
    }

    @Override
    public OJSQLBuilderForTable create()
    {
      return new OJSQLBuilderForTable(databaseType, connectionSupplier, closeConnectionAfterStatement, tableName, idColumnName);
    }
  }

  /**
   * Abstract base for the builders to create SQL statement builders.
   *
   * @param <SQLBUILDER> the type of the final SQL statement builder, which will be created by this builder
   * @param <BUILDER>    the concrete type of this builder (used for pipelining)
   */
  private static abstract class _AbstractBuilder<SQLBUILDER extends AbstractSQLBuilder, BUILDER extends _AbstractBuilder<SQLBUILDER, BUILDER>>
  {
    protected final EDatabaseType databaseType;
    protected final String idColumnName;
    protected Supplier<Connection> connectionSupplier;
    protected boolean closeConnectionAfterStatement = true;

    /**
     * Creates a new builder.
     *
     * @param pDatabaseType       the type of the database to use for the builder
     * @param pGlobalIdColumnName a global name for all id columns for this builder
     */
    private _AbstractBuilder(EDatabaseType pDatabaseType, String pGlobalIdColumnName)
    {
      databaseType = pDatabaseType;
      idColumnName = pGlobalIdColumnName;
    }

    /**
     * Creates a copy of another builder.
     *
     * @param pOther the builder to create the copy from
     */
    private _AbstractBuilder(_AbstractBuilder<?, ?> pOther)
    {
      databaseType = pOther.databaseType;
      idColumnName = pOther.idColumnName;
      connectionSupplier = pOther.connectionSupplier;
      closeConnectionAfterStatement = pOther.closeConnectionAfterStatement;
    }

    /**
     * Configures the builder to hold permanent database connection.
     * This connection will not be closed and used for all statements from the final builder.
     *
     * @param pConnection the database connection to use for the builder
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withPermanentConnection(Connection pConnection)
    {
      connectionSupplier = () -> pConnection;
      closeConnectionAfterStatement = false;
      //noinspection unchecked
      return (BUILDER) this;
    }

    /**
     * Configures the builder to obtain a new connection the database for every statement.
     * A used connection will be closed after every execution of a SQL statement.
     * The connections are based on several connection information.
     *
     * @param pConnectionInfo information for the database connection
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withClosingAndRenewingConnection(DBConnectionInfo pConnectionInfo)
    {
      return withClosingAndRenewingConnection(_createConnectionSupplier(pConnectionInfo));
    }

    /**
     * Configures the builder to obtain a new connection the database for every statement.
     * A used connection will be closed after every execution of a SQL statement.
     *
     * @param pConnectionSupplier the supplier for new connections
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withClosingAndRenewingConnection(Supplier<Connection> pConnectionSupplier)
    {
      connectionSupplier = pConnectionSupplier;
      //noinspection unchecked
      return (BUILDER) this;
    }

    /**
     * Creates the final SQL statement builder.
     *
     * @return a SQL statement builder
     */
    public abstract SQLBUILDER create();

    /**
     * Creates the supplier of a database connection, that will open a connection every time it is called.
     *
     * @param pConnectionInfo information for the database connection
     * @return a database connection supplier
     */
    private Supplier<Connection> _createConnectionSupplier(DBConnectionInfo pConnectionInfo)
    {
      final String dbUrl = pConnectionInfo.getJDBCConnectionString();
      return () -> {
        try
        {
          return pConnectionInfo.getUsername() == null || pConnectionInfo.getPassword() == null ? DriverManager.getConnection(dbUrl) :
              DriverManager.getConnection(dbUrl, pConnectionInfo.getUsername(), pConnectionInfo.getPassword());
        }
        catch (SQLException pE)
        {
          throw new OJDatabaseException("Unable to connect to the database! host = " + pConnectionInfo.getHost() +
                                            " port = " + pConnectionInfo.getPort(), pE);
        }
      };
    }
  }
}
