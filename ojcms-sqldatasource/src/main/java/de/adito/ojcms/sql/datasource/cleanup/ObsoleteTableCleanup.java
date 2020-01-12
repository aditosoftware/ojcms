package de.adito.ojcms.sql.datasource.cleanup;

import de.adito.ojcms.sql.datasource.model.PersistenceModels;
import de.adito.ojcms.sql.datasource.startup.IDatabaseCleanup;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

/**
 * Removes all obsolete bean container tables.
 * Compares the still existing tables with the actual tables within the database.
 *
 * @author Simon Danner, 02.01.2020
 */
@ApplicationScoped
class ObsoleteTableCleanup implements IDatabaseCleanup
{
  @Inject
  private PersistenceModels models;

  @Override
  public void cleanup(OJSQLBuilder pBuilder)
  {
    final List<String> allTables = pBuilder.getAllTableNames();
    allTables.removeAll(models.getAllContainerTableNames());
    //Drop all remaining/obsolete tables
    allTables.forEach(pBuilder::dropTable);
  }
}
