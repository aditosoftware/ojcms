package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.IField;
import de.adito.beans.core.fields.util.*;
import de.adito.beans.core.util.BeanReflector;
import de.adito.beans.persistence.*;
import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.IColumnIdentification;
import de.adito.beans.persistence.datastores.sql.builder.definition.column.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.IWhereCondition;
import de.adito.beans.persistence.datastores.sql.builder.result.ResultRow;
import de.adito.beans.persistence.datastores.sql.builder.util.*;
import de.adito.beans.persistence.datastores.sql.util.*;
import de.adito.beans.persistence.spi.IPersistentBean;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static de.adito.beans.persistence.datastores.sql.builder.definition.condition.IWhereCondition.*;

/**
 * Implementation of a persistent bean.
 * This persistent bean is a {@link de.adito.beans.core.EncapsulatedBuilder.IBeanEncapsulatedBuilder} to create a bean later on.
 * Single persistent beans are stored in one database table.
 * Each row represents a bean. It will be identified by a unique id. (according to {@link Persist#containerId()})
 * If a bean is added, that has more columns than all existing ones, the missing columns will be added.
 * Also, columns will be removed, when the 'largest' bean is removed.
 * The values of the fields are stored in a general serial string format, because columns types may be different.
 *
 * @param <BEAN> the type of the bean, that will be created from this persistent bean builder
 * @author Simon Danner, 21.04.2018
 */
public class SQLPersistentBean<BEAN extends IBean<BEAN>> implements IPersistentBean
{
  private static final IColumnIdentification<String> BEAN_ID_COLUMN_IDENTIFICATION =
      IColumnIdentification.of(IDatabaseConstants.BEAN_TABLE_BEAN_ID, String.class);
  private static final IColumnDefinition BEAN_ID_COLUMN_DEFINITION =
      IColumnDefinition.of(IDatabaseConstants.BEAN_TABLE_BEAN_ID, EColumnType.STRING.create().primaryKey().modifiers(EColumnModifier.NOT_NULL));
  private final IWhereCondition<String> beanIdCondition;
  private final Map<IField<?>, _ColumnIdentification<?>> columns;
  private final OJSQLBuilderForTable builder;

  /**
   * Removes all obsolete single beans from the database table and removes columns, if necessary.
   *
   * @param pConnectionInfo       the database connection information
   * @param pStillExistingBeanIds a collection of still existing single bean ids
   */
  public static void removeObsoletes(DBConnectionInfo pConnectionInfo, Collection<String> pStillExistingBeanIds)
  {
    final OJSQLBuilderForTable builder = OJSQLBuilderFactory.newSQLBuilder(pConnectionInfo.getDatabaseType(), IDatabaseConstants.ID_COLUMN)
        .forSingleTable(IDatabaseConstants.BEAN_TABLE_NAME)
        .withClosingAndRenewingConnection(pConnectionInfo)
        .create();
    builder.doDelete(pDelete -> pDelete
        .where(not(in(BEAN_ID_COLUMN_IDENTIFICATION, pStillExistingBeanIds.stream())))
        .delete());
  }

  /**
   * Creates the single bean database table, if it is not existing yet.
   * This static entry point is necessary, because the table may have to be present before the usage of a single bean. (e.g. for foreign keys)
   *
   * @param pConnectionInfo the database connection information
   */
  public static void createBeanTable(DBConnectionInfo pConnectionInfo)
  {
    final OJSQLBuilderForTable builder = OJSQLBuilderFactory.newSQLBuilder(pConnectionInfo.getDatabaseType(), IDatabaseConstants.ID_COLUMN)
        .forSingleTable(IDatabaseConstants.BEAN_TABLE_NAME)
        .withClosingAndRenewingConnection(pConnectionInfo)
        .create();
    _createTableIfNotExisting(builder);
  }

  /**
   * Creates the single bean database table, if it is not existing yet.
   * The creation is based on an existing builder.
   *
   * @param pBuilder the builder to create the table with
   */
  private static void _createTableIfNotExisting(OJSQLBuilderForTable pBuilder)
  {
    pBuilder.ifTableNotExistingCreate(pCreate -> pCreate
        .columns(BEAN_ID_COLUMN_DEFINITION)
        .create());
  }

  /**
   * Creates a persistent bean.
   *
   * @param pBeanId         the id of the bean
   * @param pBeanType       the final bean type, which will be created by this persistent bean
   * @param pConnectionInfo the database connection information
   * @param pBeanDataStore  the data store for persistent bean elements
   */
  public SQLPersistentBean(String pBeanId, Class<BEAN> pBeanType, DBConnectionInfo pConnectionInfo, BeanDataStore pBeanDataStore)
  {
    beanIdCondition = isEqual(BEAN_ID_COLUMN_IDENTIFICATION, pBeanId);
    columns = _createColumnMap(pBeanType);
    builder = OJSQLBuilderFactory.newSQLBuilder(pConnectionInfo.getDatabaseType(), IDatabaseConstants.ID_COLUMN)
        .forSingleTable(IDatabaseConstants.BEAN_TABLE_NAME)
        .withClosingAndRenewingConnection(pConnectionInfo)
        .withCustomSerializer(new BeanSQLSerializer(pBeanDataStore))
        .create();
    _createTableIfNotExisting(builder);
    _checkColumnSize();
    _checkRowExisting();
  }

  @Override
  public <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    //noinspection unchecked
    return builder.doSelectOne((IColumnIdentification<VALUE>) columns.get(pField), pSelect -> pSelect
        .where(beanIdCondition)
        .firstResult()
        .map(pValue -> pValue == null ? pField.getInitialValue() : pValue)
        .orIfNotPresentThrow(() -> new OJDatabaseException("No result for bean id " + beanIdCondition.getValue() + " found. field: " + pField)));
  }

  @Override
  public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField)
  {
    builder.doUpdate(pUpdate -> pUpdate
        .set(new _ColumnTuple<>(pField, columns.get(pField).id, pValue))
        .where(beanIdCondition)
        .update());
  }

  @Override
  public <VALUE> void removeField(IField<VALUE> pField)
  {
    throw new UnsupportedOperationException("It's not allowed to remove fields from a persistent bean!");
  }

  @NotNull
  @Override
  public Iterator<FieldTuple<?>> iterator()
  {
    final List<FieldTuple<?>> fieldTuples = builder.doSelect(pSelect -> pSelect
        .where(beanIdCondition)
        .firstResult()
        .map(this::_mapResultToFieldTuples))
        .orElseThrow(() -> new OJDatabaseException("No result for bean id " + beanIdCondition.getValue() + " found."));
    return fieldTuples.iterator();
  }

  /**
   * Transforms the result of a select query to a list of bean tuples.
   *
   * @param pResultRow the result row of the select query
   * @return a list of field value tuples
   */
  private List<FieldTuple<?>> _mapResultToFieldTuples(ResultRow pResultRow)
  {
    //noinspection unchecked
    return columns.entrySet().stream()
        .map(pEntry -> ((IField) pEntry.getKey()).newTuple(
            pResultRow.hasColumn(pEntry.getValue()) && pResultRow.get(pEntry.getValue()) != null ? pResultRow.get(pEntry.getValue()) :
                pEntry.getKey().getInitialValue()))
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
  }

  /**
   * Creates a mapping from bean field to column identification for this persistent bean.
   *
   * @param pBeanType the type of the bean
   * @return the mapping
   */
  private Map<IField<?>, _ColumnIdentification<?>> _createColumnMap(Class<BEAN> pBeanType)
  {
    final AtomicInteger id = new AtomicInteger();
    //noinspection unchecked
    return BeanReflector.reflectBeanFields(pBeanType).stream()
        .collect(LinkedHashMap::new, (pMap, pField) -> pMap.put(pField, new _ColumnIdentification(pField, id.getAndIncrement())), Map::putAll);
  }

  /**
   * Checks, if columns must be added.
   */
  private void _checkColumnSize()
  {
    IntStream.range(builder.getColumnCount() - 1, columns.size())
        .mapToObj(pIndex -> IDatabaseConstants.BEAN_TABLE_COLUMN_PREFIX + pIndex)
        .forEach(pColumnName -> builder.addColumn(IColumnDefinition.of(pColumnName, EColumnType.STRING.create())));
  }

  /**
   * Checks, if the row for this bean has to be inserted.
   */
  private void _checkRowExisting()
  {
    if (builder.doSelect(pSelect -> pSelect
        .where(beanIdCondition)
        .countRows() == 0))
      builder.doInsert(pInsert -> pInsert
          .values(beanIdCondition)
          .insert());
  }

  /**
   * Column identification for this special bean field columns.
   * The column name is built from a prefix and the index of the column.
   *
   * @param <VALUE> the data type of the associated bean field
   */
  private class _ColumnIdentification<VALUE> implements IColumnIdentification<VALUE>, IBeanFieldBased<VALUE>
  {
    private final IField<VALUE> beanField;
    private final int id;

    /**
     * Creates a new column identification.
     *
     * @param pBeanField the bean field associated with the column
     * @param pId        the index of the column
     */
    private _ColumnIdentification(IField<VALUE> pBeanField, int pId)
    {
      beanField = pBeanField;
      id = pId;
    }

    @Override
    public String getColumnName()
    {
      return IDatabaseConstants.BEAN_TABLE_COLUMN_PREFIX + id;
    }

    @Override
    public Class<VALUE> getDataType()
    {
      return beanField.getDataType();
    }

    @Override
    public IField<VALUE> getBeanField()
    {
      return beanField;
    }
  }

  /**
   * A column value tuple for this special database table.
   * The column name is built from a prefix and the index of the column.
   *
   * @param <VALUE> the data type of the associated bean tuple
   */
  private class _ColumnTuple<VALUE> extends BeanColumnValueTuple<VALUE>
  {
    private final IColumnIdentification<VALUE> column;

    /**
     * Creates a new column value tuple.
     *
     * @param pBeanField the associated bean field
     * @param pId        the index of the column
     * @param pValue     the data value for the column
     */
    private _ColumnTuple(IField<VALUE> pBeanField, int pId, VALUE pValue)
    {
      super(pBeanField.newTuple(pValue));
      column = IColumnIdentification.of(IDatabaseConstants.BEAN_TABLE_COLUMN_PREFIX + pId, pBeanField.getDataType(), (pName, pType) -> false);
    }

    @Override
    public IColumnIdentification<VALUE> getColumn()
    {
      return column;
    }
  }
}
