package de.adito.ojcms.sql.datasource.startup;

import de.adito.ojcms.cdi.startup.IStartupCallback;
import de.adito.ojcms.sql.datasource.connection.GlobalBuilder;
import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Database initialization that will be executed on application startup.
 * Creates the single bean table if necessary.
 * Then performs all {@link IDatabaseCleanup}.
 * Finally initializes all still existing {@link PersistenceModels}.
 *
 * @author Simon Danner, 02.01.2020
 */
@ApplicationScoped
class DatabaseInitialization implements IStartupCallback
{
  @Inject
  private PersistenceModels models;
  @Inject
  private Instance<IDatabaseCleanup> cleaners;
  @Inject
  @GlobalBuilder
  private OJSQLBuilder builder;

  @Override
  public void onCdiStartup()
  {
    if (!models.getAllSingleBeanIds().isEmpty())
      SingleBeanPersistenceModel.createSingleBeanTableIfNecessary(builder);

    for (IDatabaseCleanup cleaner : cleaners)
      cleaner.cleanup(builder);

    models.initAllModels(builder);
  }
}
