package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.sql.datasource.model.column.*;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.column.*;
import de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition;
import de.adito.ojcms.sqlbuilder.result.ResultRow;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.exceptions.BeanDataNotFoundException;
import de.adito.ojcms.utils.StringUtility;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.INDEX_COLUMN_NAME;
import static de.adito.ojcms.sqlbuilder.definition.ENumericOperation.*;
import static de.adito.ojcms.sqlbuilder.definition.INumericValueAdaption.of;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.greaterThan;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.greaterThanOrEqual;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.isEqual;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereConditionsForId.create;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.greaterThan;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.lessThan;
import static java.util.function.Function.identity;

/**
 * The persistence model for a database table for a persistent bean container.
 * Provides meta information to initialize the database table.
 * Also handles the requesting of bean data and processing of changes.
 *
 * @author Simon Danner, 01.01.2020
 */
public class ContainerPersistenceModel implements IPersistenceModel
{
  private static final IColumnDefinition INDEX_COLUMN_DEFINITION = IColumnDefinition.of(INDEX_COLUMN_NAME, EColumnType.INT.create());
  private static final IColumnIdentification<Integer> INDEX_COLUMN = IColumnIdentification.of(INDEX_COLUMN_NAME, Integer.class);

  private final String containerId;
  private final BeanColumnDefinition[] columnDefinitions;
  private final BeanColumnIdentification<?>[] beanColumns;
  private final List<IColumnIdentification<?>> columnsToSelect;

  /**
   * Initializes the persistence model for a persistent bean container.
   *
   * @param pContainerId the container id of the persistent container
   * @param pBeanType    the type of beans within the bean container
   */
  ContainerPersistenceModel(String pContainerId, Class<? extends IBean> pBeanType)
  {
    containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
    final List<IField<?>> beanFields = BeanReflector.reflectBeanFields(pBeanType);
    columnDefinitions = BeanColumnDefinition.ofFields(beanFields);
    beanColumns = BeanColumnIdentification.ofMultiple(beanFields);
    columnsToSelect = new ArrayList<>();
    columnsToSelect.add(INDEX_COLUMN);
    columnsToSelect.addAll(Arrays.asList(beanColumns));
  }

  @Override
  public void initModelInDatabase(OJSQLBuilder pBuilder)
  {
    final List<IColumnDefinition> columnsToCreate = new ArrayList<>();
    columnsToCreate.add(INDEX_COLUMN_DEFINITION);
    columnsToCreate.addAll(Arrays.asList(columnDefinitions));

    pBuilder.ifTableNotExistingCreate(containerId, pCreate -> pCreate
        .withIdColumn()
        .columns(columnsToCreate)
        .create());
  }

  /**
   * Loads the amount of bean data within the database table.
   *
   * @param pBuilder a builder to execute SQL statements
   * @return size amount of bean data in this container
   */
  public int loadSize(OJSQLBuilder pBuilder)
  {
    return pBuilder.doSelect(pSelect -> pSelect.from(containerId).countRows());
  }

  /**
   * Loads persistent bean data from a container by index.
   *
   * @param pKey     the index based to key to identify the bean to load
   * @param pBuilder a builder to execute SQL statements
   * @return the loaded persistent bean data
   */
  public PersistentBeanData loadDataByIndex(InitialIndexKey pKey, OJSQLBuilder pBuilder)
  {
    return pBuilder.doSelect(pSelect -> pSelect
        .select(beanColumns)
        .from(containerId)
        .where(isEqual(INDEX_COLUMN, pKey.getIndex()))
        .firstResult()
        .map(pRow -> pRow.toMap(beanColumns, (Function<BeanColumnIdentification<?>, IField<?>>) BeanColumnIdentification::getBeanField))
        .map(pBeanContent -> new PersistentBeanData(pKey.getIndex(), pBeanContent)))
        .orElseThrow(() -> new BeanDataNotFoundException(pKey));
  }

  /**
   * Tries to load {@link PersistentBeanData} from the container by identifying field value tuples.
   *
   * @param pIdentifiers field value tuples to identify the bean to load
   * @param pBuilder     a builder to execute SQL statements
   * @return the requested bean data
   */
  public Optional<PersistentBeanData> loadDataByIdentifiers(Map<IField<?>, Object> pIdentifiers, OJSQLBuilder pBuilder)
  {
    return pBuilder.doSelect(pSelect -> pSelect
        .select(columnsToSelect)
        .withId()
        .from(containerId)
        .where(BeanWhereCondition.conditionsOfMap(pIdentifiers))
        .firstResult()
        .map(this::_toBeanData));
  }

  /**
   * Performs a full data load for the persistent bean container.
   * Might be very computation intensive. The caller should use this only in rare cases.
   *
   * @param pBuilder a builder to execute SQL statements
   * @return all bean data within this container mapped by index
   */
  public Map<Integer, PersistentBeanData> loadFullData(OJSQLBuilder pBuilder)
  {
    return pBuilder.doSelect(pSelect -> pSelect
        .select(columnsToSelect)
        .withId()
        .from(containerId)
        .fullResult()
        .stream()
        .map(this::_toBeanData)
        .collect(Collectors.toMap(PersistentBeanData::getIndex, identity())));
  }

  /**
   * Process values changes of a bean within the persistent bean container.
   *
   * @param pIndex         the index of the bean the values have been changed
   * @param pChangedValues the changed value as field value tuples
   * @param pBuilder       a builder to execute SQL statements
   */
  public void processValueChanges(int pIndex, Map<IField<?>, Object> pChangedValues, OJSQLBuilder pBuilder)
  {
    pBuilder.doUpdate(pUpdate -> pUpdate
        .table(containerId)
        .set(BeanColumnValueTuple.ofMap(pChangedValues))
        .where(isEqual(INDEX_COLUMN, pIndex))
        .update());
  }

  /**
   * Processes bean additions for the persistent bean container.
   *
   * @param pNewBeans the data of newly added beans
   * @param pBuilder  a builder to execute SQL statements
   */
  public void processAdditions(Set<PersistentBeanData> pNewBeans, OJSQLBuilder pBuilder)
  {
    for (PersistentBeanData newBean : pNewBeans)
    {
      //Increase index of every row above the inserted index
      pBuilder.doUpdate(pUpdate -> pUpdate
          .table(containerId)
          .adaptNumericValue(of(INDEX_COLUMN, ADD, 1))
          .where(greaterThanOrEqual(INDEX_COLUMN, newBean.getIndex()))
          .update());

      pBuilder.doInsert(pInsert -> pInsert
          .into(containerId)
          .values(_tuplesToInsertForNewBean(newBean))
          .insert());
    }
  }

  /**
   * Processes bean removals for the persistent bean container.
   *
   * @param pKeysToRemove a collection of index based keys to remove
   * @param pBuilder      a builder to execute SQL statements
   */
  public void processRemovals(Set<InitialIndexKey> pKeysToRemove, OJSQLBuilder pBuilder)
  {
    final List<Integer> indexesToDelete = pKeysToRemove.stream()
        .map(InitialIndexKey::getIndex)
        .sorted()
        .collect(Collectors.toList());

    pBuilder.doDelete(pDelete -> pDelete
        .from(containerId)
        .where(IWhereCondition.in(INDEX_COLUMN, indexesToDelete))
        .delete());

    //Update all indexes between the deleted indexes
    for (int i = 0; i < indexesToDelete.size() - 1; i++)
    {
      final int indexToDelete = indexesToDelete.get(i);
      final int nextIndexToDelete = indexesToDelete.get(i + 1);
      final int offset = i + 1;

      pBuilder.doUpdate(pUpdate -> pUpdate
          .table(containerId)
          .adaptNumericValue(of(INDEX_COLUMN, SUBTRACT, offset))
          .whereId(create(greaterThan(), indexToDelete).and(lessThan(), nextIndexToDelete))
          .update());
    }

    //Update all row indexes after the last index to delete
    pBuilder.doUpdate(pUpdate -> pUpdate
        .table(containerId)
        .adaptNumericValue(of(INDEX_COLUMN, SUBTRACT, indexesToDelete.size()))
        .where(greaterThan(INDEX_COLUMN, indexesToDelete.get(indexesToDelete.size() - 1)))
        .update());
  }

  /**
   * Converts a SQL {@link ResultRow} to {@link PersistentBeanData}.
   *
   * @param pResultRow the SQL result row to convert
   * @return the converted persistent bean data
   */
  private PersistentBeanData _toBeanData(ResultRow pResultRow)
  {
    final Map<IField<?>, Object> beanContent = pResultRow.toMap(beanColumns, BeanColumnIdentification::getBeanField);
    final int index = pResultRow.get(INDEX_COLUMN);
    return new PersistentBeanData(index, beanContent);
  }

  /**
   * Creates a list of {@link IColumnValueTuple} to perform an insertion based on some {@link PersistentBeanData}.
   * The index value tuple will be included as well.
   *
   * @param pBeanData the persistent bean data to create column value tuples for
   * @return a list of column value tuples for an insertion
   */
  private List<IColumnValueTuple<?>> _tuplesToInsertForNewBean(PersistentBeanData pBeanData)
  {
    final List<IColumnValueTuple<?>> tuplesToInsert = new ArrayList<>();
    tuplesToInsert.add(IColumnValueTuple.of(INDEX_COLUMN, pBeanData.getIndex()));
    tuplesToInsert.addAll(BeanColumnValueTuple.ofMap(pBeanData.getData()));
    return tuplesToInsert;
  }
}
