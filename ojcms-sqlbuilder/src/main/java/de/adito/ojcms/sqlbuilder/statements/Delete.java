package de.adito.ojcms.sqlbuilder.statements;

import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.condition.*;
import de.adito.ojcms.sqlbuilder.format.*;

import java.util.*;
import java.util.stream.Collectors;

import static de.adito.ojcms.sqlbuilder.definition.ENumericOperation.*;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.*;
import static de.adito.ojcms.sqlbuilder.format.EFormatter.*;

/**
 * A delete statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Delete extends AbstractSQLStatement<WhereModifiers, Boolean, Boolean, Delete>
{
  /**
   * Creates a new delete statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Delete(IStatementExecutor<Boolean> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, new WhereModifiers(), pIdColumnName);
  }

  /**
   * Performs the deletion.
   *
   * @return <tt>true</tt>, if the deletion was successful
   */
  public boolean delete()
  {
    return doQuery();
  }

  @Override
  protected Boolean doQuery()
  {
    _IdArranger idArranger = new _IdArranger(); //Store ids to delete before the deletion
    final StatementFormatter deleteStatement = DELETE.create(databaseType, idColumnIdentification.getColumnName())
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    final Boolean result = executeStatement(deleteStatement);
    idArranger.rearrangeIds(); //Rearrange after the deletion
    return result;
  }

  /**
   * Rearranges the ids after a deletion.
   * If there is no id column, no work must be done.
   */
  private class _IdArranger
  {
    private final Optional<List<Integer>> deletedIds;

    /**
     * Creates the rearranger and stores all ids that will be deleted beforehand.
     */
    private _IdArranger()
    {
      if (!isIdColumnPresent())
        deletedIds = Optional.empty();
      else
        deletedIds = Optional.of(builder.doSelectId(pSelect -> pSelect
            .whereId(modifiers.getWhereIdCondition().orElse(null))
            .where(modifiers.getWhereCondition().orElse(null))
            .fullResult()
            .stream()
            .collect(Collectors.toList())));
    }

    /**
     * Rearranges the remaining ids to be in a consecutive order again.
     */
    void rearrangeIds()
    {
      deletedIds.ifPresent(pDeletedIds -> {
        //update all ranges between two ids to delete
        for (int i = 0; i < pDeletedIds.size() - 1; i++)
        {
          final int offset = i;
          builder.doUpdate(pUpdate -> pUpdate
              .adaptId(SUBTRACT, offset + 1)
              .whereId(IWhereConditionsForId.create(greaterThan(), pDeletedIds.get(offset))
                           .and(lessThan(), pDeletedIds.get(offset + 1)))
              .update());
        }
        //update all rows after the last id to delete
        builder.doUpdate(pUpdate -> pUpdate
            .adaptId(SUBTRACT, 1)
            .whereId(greaterThan(), pDeletedIds.get(pDeletedIds.size() - 1))
            .update());
      });
    }
  }
}
