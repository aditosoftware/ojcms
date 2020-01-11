package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.*;
import de.adito.ojcms.sqlbuilder.serialization.*;

import java.util.function.Function;

/**
 * Factory for the SQL statement builders {@link OJSQLBuilder} and {@link OJSQLBuilderForTable}.
 * This is the only public entry point for the SQL builder framework.
 * The builders can be configured in a functional way.
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
   * If a SQL builder for a single database table is necessary,
   * {@link Builder#forSingleTable(String)} can be used to create an {@link OJSQLBuilderForTable}
   *
   * @param pPlatform           the database platform to use for the builder
   * @param pGlobalIdColumnName a global name for all id columns for this builder
   * @return a builder to configure the SQL statement builder
   */
  public static Builder newSQLBuilder(IDatabasePlatform pPlatform, String pGlobalIdColumnName)
  {
    return new Builder(pPlatform, pGlobalIdColumnName);
  }

  /**
   * Creates a new builder to configure a SQL statement builder.
   * This builder will be based on an existing SQL builder, which can be adapted by the builder.
   * The default {@link Builder} is used to create an {@link OJSQLBuilder}.
   * If a sql builder for a single database table is necessary,
   * {@link Builder#forSingleTable(String)} can be used to create an {@link OJSQLBuilderForTable}
   *
   * @param pExistingBuilder an existing builder to take the information from
   * @return a builder to configure the SQL statement builder
   */
  public static Builder newSQLBuilder(AbstractSQLBuilder pExistingBuilder)
  {
    final Builder builder = newSQLBuilder(pExistingBuilder.getDatabasePlatform(), pExistingBuilder.getIdColumnName());
    builder.connectionSupplier = pExistingBuilder.getPlatformConnectionSupplier();
    builder.closeConnectionAfterStatement = pExistingBuilder.closeAfterStatement();
    builder.serializer = pExistingBuilder.getSerializer();
    return builder;
  }

  /**
   * A builder for the SQL statement builder to create a {@link OJSQLBuilder}.
   */
  public static class Builder extends _AbstractBuilder<OJSQLBuilder, Builder>
  {
    /**
     * Creates a new builder with the required information.
     *
     * @param pPlatform     the database platform to use for the builder
     * @param pIdColumnName a global name for all id columns for this builder
     */
    private Builder(IDatabasePlatform pPlatform, String pIdColumnName)
    {
      super(pPlatform, pIdColumnName);
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
      return new OJSQLBuilderImpl(databasePlatform, connectionSupplier, closeConnectionAfterStatement, serializer, idColumnName);
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
     * Configures the SQL builder for multiple database tables.
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
      return new OJSQLBuilderForTableImpl(databasePlatform, connectionSupplier, closeConnectionAfterStatement, serializer, tableName,
                                          idColumnName);
    }
  }

  /**
   * Abstract base class for the builders to create SQL statement builders.
   *
   * @param <SQLBUILDER> the type of the final SQL statement builder, which will be created by this builder
   * @param <BUILDER>    the concrete type of this builder (used for pipelining)
   */
  private abstract static class _AbstractBuilder<SQLBUILDER extends IBaseBuilder, BUILDER extends _AbstractBuilder<SQLBUILDER, BUILDER>>
  {
    protected final IDatabasePlatform databasePlatform;
    protected final String idColumnName;
    protected IDatabaseConnectionSupplier connectionSupplier;
    protected IValueSerializer serializer = new DefaultValueSerializer();
    protected boolean closeConnectionAfterStatement = true;

    /**
     * Creates a new builder.
     *
     * @param pPlatform           the database platform to use for the builder
     * @param pGlobalIdColumnName a global name for all id columns for this builder
     */
    private _AbstractBuilder(IDatabasePlatform pPlatform, String pGlobalIdColumnName)
    {
      databasePlatform = pPlatform;
      idColumnName = pGlobalIdColumnName;
    }

    /**
     * Creates a copy of another builder.
     *
     * @param pOther the builder to create the copy from
     */
    private _AbstractBuilder(_AbstractBuilder<?, ?> pOther)
    {
      databasePlatform = pOther.databasePlatform;
      idColumnName = pOther.idColumnName;
      connectionSupplier = pOther.connectionSupplier;
      serializer = pOther.serializer;
      closeConnectionAfterStatement = pOther.closeConnectionAfterStatement;
    }

    /**
     * Configures the builder to hold permanent database connection.
     * This connection will not be closed and used for all statements from the final builder.
     *
     * @param pConnSupplierResolver a function the resolve a db connection supplier from a factory for platform dependent instances
     * @param pAutoCommit           <tt>true</tt> if auto commit should be activated for the connection
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withPermanentConnection(Function<ConnectionSupplierFactory, IDatabaseConnectionSupplier> pConnSupplierResolver,
                                           boolean pAutoCommit)
    {
      return withPermanentConnection(pConnSupplierResolver.apply(new ConnectionSupplierFactory(pAutoCommit)));
    }

    /**
     * Configures the builder to hold permanent database connection.
     * This connection will not be closed and used for all statements from the final builder.
     *
     * @param pConnectionSupplier a platform dependent supplier for database connections
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withPermanentConnection(IDatabaseConnectionSupplier pConnectionSupplier)
    {
      connectionSupplier = pConnectionSupplier;
      closeConnectionAfterStatement = false;
      //noinspection unchecked
      return (BUILDER) this;
    }

    /**
     * Configures the builder to obtain a new connection to the database for every statement.
     * An used connection will be closed after every execution of a SQL statement.
     * The connections are based on several connection information.
     *
     * @param pConnSupplierResolver a function the resolve a db connection supplier from a factory for platform dependent instances
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withClosingAndRenewingConnection(Function<ConnectionSupplierFactory, IDatabaseConnectionSupplier> pConnSupplierResolver)
    {
      return withClosingAndRenewingConnection(pConnSupplierResolver.apply(new ConnectionSupplierFactory(true)));
    }

    /**
     * Configures the builder to obtain a new connection to the database for every statement.
     * An used connection will be closed after every execution of a SQL statement.
     * The connections are based on several connection information.
     *
     * @param pConnectionSupplier a platform dependent supplier for database connections
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withClosingAndRenewingConnection(IDatabaseConnectionSupplier pConnectionSupplier)
    {
      connectionSupplier = pConnectionSupplier;
      //noinspection unchecked
      return (BUILDER) this;
    }

    /**
     * Configures the builder to use a custom value serializer for all statements.
     *
     * @param pSerializer the value serializer
     * @return the builder itself to enable a pipelining mechanism
     */
    public BUILDER withCustomSerializer(IValueSerializer pSerializer)
    {
      serializer = pSerializer;
      //noinspection unchecked
      return (BUILDER) this;
    }

    /**
     * Creates the final SQL statement builder.
     *
     * @return a SQL statement builder
     */
    public abstract SQLBUILDER create();
  }
}
