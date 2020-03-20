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

import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.*;
import static de.adito.ojcms.sqlbuilder.definition.ENumericOperation.*;
import static de.adito.ojcms.sqlbuilder.definition.INumericValueAdaption.of;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.greaterThan;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.greaterThanOrEqual;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.isEqual;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereConditionsForId.create;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.greaterThan;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.lessThan;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;

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
  protected static final IColumnIdentification<Integer> INDEX_COLUMN = IColumnIdentification.of(INDEX_COLUMN_NAME, Integer.class);

  protected final String containerId;
  private final Set<IColumnDefinition> columnDefinitions;
  private final Set<BeanColumnIdentification<?>> beanColumnIdentifications;
  private final Set<IColumnIdentification<?>> columnsToSelect;

  /**
   * Initializes the persistence model for a persistent bean container.
   *
   * @param pContainerId the container id of the persistent container
   * @param pBeanType    the types of the beans in the container
   */
  ContainerPersistenceModel(String pContainerId, Class<? extends IBean> pBeanType)
  {
    this(pContainerId, BeanColumnDefinition.ofFields(BeanReflector.reflectBeanFields(pBeanType)));
  }

  /**
   * Initializes the persistence model for a persistent bean container.
   *
   * @param pContainerId      the container id of the persistent container
   * @param pColumns          the bean based column definitions for this container model
   * @param pAdditionsColumns optional additional columns to select
   */
  ContainerPersistenceModel(String pContainerId, Set<BeanColumnDefinition<?>> pColumns, IColumnIdentification<?>... pAdditionsColumns)
  {
    containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
    columnDefinitions = new HashSet<>(pColumns);
    beanColumnIdentifications = pColumns.stream().map(BeanColumnDefinition::toColumnIdentification).collect(toSet());

    columnsToSelect = new HashSet<>();
    columnsToSelect.add(INDEX_COLUMN);
    columnsToSelect.addAll(beanColumnIdentifications);
    columnsToSelect.addAll(Arrays.asList(pAdditionsColumns));
  }

  @Override
  public void initModelInDatabase(OJSQLBuilder pBuilder)
  {
    if (!pBuilder.hasTable(containerId))
      pBuilder.doCreate(pCreate -> pCreate //
          .tableName(containerId) //
          .withIdColumn() //
          .columns(getColumnsToCreateInitially()) //
          .create());
    else
    {
      final Set<String> existingColumnNames = pBuilder.getAllColumnNames(containerId);

      final Set<IColumnDefinition> columnsToAdd = columnDefinitions.stream() //
          .filter(pColumn -> !existingColumnNames.contains(pColumn.getColumnName())) //
          .collect(toSet());

      //After the removal the remaining columns must be dropped
      existingColumnNames.remove(ID_COLUMN);
      existingColumnNames.remove(INDEX_COLUMN_NAME);
      existingColumnNames.remove(BEAN_TYPE_COLUMN_NAME);
      existingColumnNames.removeAll(columnDefinitions.stream() //
          .map(IColumnDefinition::getColumnName) //
          .collect(toSet()));

      if (existingColumnNames.isEmpty() && columnsToAdd.isEmpty())
        return;

      //Add new columns and drop obsolete columns
      pBuilder.doAlterTable(pAlter -> pAlter //
          .table(containerId) //
          .columnsToAdd(columnsToAdd) //
          .columnsToDrop(existingColumnNames) //
          .alter());
    }
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
    return selectSingleResultByIndex(pKey, columnsToSelect, pBuilder, this::_toBeanData);
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
    return pBuilder.doSelect(pSelect -> pSelect //
        .select(columnsToSelect) //
        .from(containerId) //
        .where(BeanWhereCondition.conditionsOfMap(pIdentifiers)) //
        .firstResult() //
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
    return pBuilder.doSelect(pSelect -> pSelect //
        .select(columnsToSelect)//
        .from(containerId) //
        .fullResult() //
        .stream() //
        .map(this::_toBeanData) //
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
    pBuilder.doUpdate(pUpdate -> pUpdate //
        .table(containerId) //
        .set(BeanColumnValueTuple.ofMap(pChangedValues)) //
        .where(isEqual(INDEX_COLUMN, pIndex)) //
        .update());
  }

  /**
   * Processes bean additions for the persistent bean container.
   *
   * @param pBeanAdditions data of all added beans
   * @param pBuilder       a builder to execute SQL statements
   */
  public void processAdditions(Set<BeanAddition> pBeanAdditions, OJSQLBuilder pBuilder)
  {
    for (BeanAddition addition : pBeanAdditions)
    {
      //Increase index of every row above the inserted index
      pBuilder.doUpdate(pUpdate -> pUpdate //
          .table(containerId) //
          .adaptNumericValue(of(INDEX_COLUMN, ADD, 1)) //
          .where(greaterThanOrEqual(INDEX_COLUMN, addition.getIndex())) //
          .update());

      pBuilder.doInsert(pInsert -> pInsert //
          .into(containerId) //
          .values(tuplesToInsertForNewBean(addition)) //
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
    final List<Integer> indexesToDelete = pKeysToRemove.stream() //
        .map(InitialIndexKey::getIndex) //
        .sorted() //
        .collect(Collectors.toList());

    pBuilder.doDelete(pDelete -> pDelete //
        .from(containerId) //
        .where(IWhereCondition.in(INDEX_COLUMN, indexesToDelete)) //
        .delete());

    //Update all indexes between the deleted indexes
    for (int i = 0; i < indexesToDelete.size() - 1; i++)
    {
      final int indexToDelete = indexesToDelete.get(i);
      final int nextIndexToDelete = indexesToDelete.get(i + 1);
      final int offset = i + 1;

      pBuilder.doUpdate(pUpdate -> pUpdate //
          .table(containerId) //
          .adaptNumericValue(of(INDEX_COLUMN, SUBTRACT, offset)) //
          .whereId(create(greaterThan(), indexToDelete).and(lessThan(), nextIndexToDelete)) //
          .update());
    }

    //Update all row indexes after the last index to delete
    pBuilder.doUpdate(pUpdate -> pUpdate //
        .table(containerId) //
        .adaptNumericValue(of(INDEX_COLUMN, SUBTRACT, indexesToDelete.size())) //
        .where(greaterThan(INDEX_COLUMN, indexesToDelete.get(indexesToDelete.size() - 1))) //
        .update());
  }

  /**
   * Selects a generic result from a single row in the database by index.
   * Throws a {@link BeanDataNotFoundException} if there is not data at the given index.
   *
   * @param pIndexKey        the index based key to determine the row to select
   * @param pColumnsToSelect the columns to select from the single row
   * @param pBuilder         a builder to execute SQL statements
   * @param pResultMapper    a mapper to convert the single {@link ResultRow} to the requested result
   * @return the generic result for the single row
   */
  protected <RESULT> RESULT selectSingleResultByIndex(InitialIndexKey pIndexKey, Set<IColumnIdentification<?>> pColumnsToSelect,
                                                      OJSQLBuilder pBuilder, Function<ResultRow, RESULT> pResultMapper)
  {
    return pBuilder.doSelect(pSelect -> pSelect //
        .select(pColumnsToSelect) //
        .from(containerId) //
        .where(isEqual(INDEX_COLUMN, pIndexKey.getIndex())) //
        .firstResult() //
        .map(pResultMapper) //
        .orElseThrow(() -> new BeanDataNotFoundException(pIndexKey)));
  }

  /**
   * Resolves a list of initial {@link IColumnDefinition} for the container SQL table to create.
   *
   * @return the list of columns to create for the container model
   */
  protected List<IColumnDefinition> getColumnsToCreateInitially()
  {
    final List<IColumnDefinition> columnsToCreate = new ArrayList<>();
    columnsToCreate.add(INDEX_COLUMN_DEFINITION);
    columnsToCreate.addAll(columnDefinitions);
    return columnsToCreate;
  }

  /**
   * Creates a list of {@link IColumnValueTuple} to perform an insertion based on some {@link PersistentBeanData}.
   * The index value tuple will be included as well.
   *
   * @param pBeanAddition data describing the addition
   * @return a list of column value tuples for an insertion
   */
  protected List<IColumnValueTuple<?>> tuplesToInsertForNewBean(BeanAddition pBeanAddition)
  {
    final List<IColumnValueTuple<?>> tuplesToInsert = new ArrayList<>();
    tuplesToInsert.add(IColumnValueTuple.of(INDEX_COLUMN, pBeanAddition.getIndex()));
    tuplesToInsert.addAll(BeanColumnValueTuple.ofMap(pBeanAddition.getData()));
    return tuplesToInsert;
  }

  /**
   * Resolves bean content (field values tuples) from a {@link ResultRow}.
   *
   * @param pResultRow the result row to obtain bean data from
   * @return the beans's content as field value tuples
   */
  protected Map<IField<?>, Object> resultRowToBeanContent(ResultRow pResultRow)
  {
    return pResultRow.toMap(beanColumnIdentifications, BeanColumnIdentification::getBeanField);
  }

  /**
   * Converts a SQL {@link ResultRow} to {@link PersistentBeanData}.
   *
   * @param pResultRow the SQL result row to convert
   * @return the converted persistent bean data
   */
  private PersistentBeanData _toBeanData(ResultRow pResultRow)
  {
    final Map<IField<?>, Object> beanContent = resultRowToBeanContent(pResultRow);
    final int index = pResultRow.get(INDEX_COLUMN);
    return new PersistentBeanData(index, beanContent);
  }
}
