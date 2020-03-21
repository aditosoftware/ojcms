package de.adito.ojcms.sql.datasource.cleanup;

import de.adito.ojcms.sql.datasource.model.PersistenceModels;
import de.adito.ojcms.sql.datasource.startup.IDatabaseCleanup;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static de.adito.ojcms.sql.datasource.model.SingleBeanPersistenceModel.ID_COLUMN;
import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.BEAN_TABLE_NAME;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.*;

/**
 * Deletes all obsolete rows within the persistent single bean table.
 * In other words: Removes all rows which's ids are not contained in the still existing bean ids.
 *
 * @author Simon Danner, 02.01.2020
 */
@ApplicationScoped
class SingleBeanRowCleanup implements IDatabaseCleanup
{
  @Inject
  private PersistenceModels models;

  @Override
  public void cleanup(OJSQLBuilder pBuilder)
  {
    if (!pBuilder.hasTable(BEAN_TABLE_NAME))
      return;

    final List<String> stillExistingIds = models.getAllSingleBeanIds();

    if (stillExistingIds.isEmpty())
      //Then delete all
      pBuilder.doDelete(pDelete -> pDelete.from(BEAN_TABLE_NAME).delete());
    else
      //Else delete all that aren't existing anymore
      pBuilder.doDelete(pDelete -> pDelete //
          .from(BEAN_TABLE_NAME) //
          .where(not(in(ID_COLUMN, stillExistingIds))) //
          .delete());
  }
}
