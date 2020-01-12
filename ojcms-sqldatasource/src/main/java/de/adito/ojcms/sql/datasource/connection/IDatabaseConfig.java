package de.adito.ojcms.sql.datasource.connection;

import de.adito.ojcms.sql.datasource.util.DatabaseConstants;
import de.adito.ojcms.sqlbuilder.platform.connection.*;

import javax.enterprise.context.ApplicationScoped;

/**
 * Defines a database configuration that should be implemented by a module using this SQL datasource.
 * This config provides connection related data and some meta information.
 * <p>
 * Generally, the implementing type should be an {@link ApplicationScoped} CDI bean.
 *
 * @author Simon Danner, 29.12.2019
 */
public interface IDatabaseConfig
{
  /**
   * Creates a {@link IDatabaseConnectionSupplier} from a {@link ConnectionSupplierFactory}.
   *
   * @param pSupplierFactory the factory to create connection suppliers with
   * @return the connection supplier to use for the SQL datasource
   */
  IDatabaseConnectionSupplier createConnectionSupplier(ConnectionSupplierFactory pSupplierFactory);

  /**
   * The name for the id columns created by the SQL builders.
   *
   * @return the name of the id columns
   */
  default String getDefaultIdColumnName()
  {
    return DatabaseConstants.ID_COLUMN;
  }
}
