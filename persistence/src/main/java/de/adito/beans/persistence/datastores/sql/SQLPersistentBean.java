package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.BeanReflector;
import de.adito.beans.persistence.Persist;
import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.result.ResultRow;
import de.adito.beans.persistence.datastores.sql.builder.util.*;
import de.adito.beans.persistence.spi.IPersistentBean;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

/**
 * Implementation of a persistent bean.
 * This persistent bean is a {@link de.adito.beans.core.EncapsulatedBuilder.IBeanEncapsulatedBuilder} to create a bean later on.
 * Single persistent beans are stored in one database table.
 * Each row represents a bean. It will be identified by a unique id. (according to {@link Persist#containerId()})
 * If a bean is added, that has more columns than existing, the missing columns will be added.
 * Also columns will be removed, when the 'largest' bean is removed.
 * The values of the fields are stored in a general serial string format, because columns types may be different.
 *
 * @param <BEAN> the type of the bean, that will be created from this persistent bean builder
 * @author Simon Danner, 21.04.2018
 */
public class SQLPersistentBean<BEAN extends IBean<BEAN>> implements IPersistentBean
{
  private final IColumnValueTuple<String> beanIdCondition;
  private final Map<IField<?>, _ColumnIdentification<?>> columns;
  private final OJSQLBuilderForTable builder;

  /**
   * Creates a persistent bean.
   *
   * @param pBeanId       the id of the bean
   * @param pBeanType     the final bean type, which will be created by this persistent bean
   * @param pDatabaseType the database type used for this persistent bean
   * @param pHost         the host address of the database to connect to
   * @param pPort         the port of the database to connect to
   * @param pDatabaseName the name of the database to connect to
   * @param pUserName     an optional username to use for the database connection
   * @param pPassword     an optional password to use for the database connection
   */
  public SQLPersistentBean(String pBeanId, Class<BEAN> pBeanType, EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName,
                           @Nullable String pUserName, @Nullable String pPassword)
  {
    IColumnDefinition beanIdColumnDefinition = IColumnDefinition.of(IDatabaseConstants.BEAN_TABLE_BEAN_ID, EColumnType.VARCHAR, 255,
                                                                    EColumnModifier.PRIMARY_KEY, EColumnModifier.NOT_NULL);
    beanIdCondition = IColumnValueTuple.of(beanIdColumnDefinition, pBeanId);
    columns = _createColumnMap(pBeanType);
    builder = OJSQLBuilderFactory.newSQLBuilder(pDatabaseType, IDatabaseConstants.ID_COLUMN)
        .forSingleTable(IDatabaseConstants.BEAN_TABLE_NAME)
        .withClosingAndRenewingConnection(pHost, pPort, pDatabaseName, pUserName, pPassword)
        .create();
    builder.ifTableNotExistingCreate(pCreate -> pCreate
        .columns(beanIdColumnDefinition)
        .create());
    _checkColumnSize();
    _checkRowExisting();
  }

  @Override
  public <TYPE> TYPE getValue(IField<TYPE> pField)
  {
    //noinspection unchecked
    return builder.doSelectOne((IColumnIdentification<TYPE>) columns.get(pField), pSelect -> pSelect
        .where(beanIdCondition)
        .firstResult()
        .orIfNotPresentThrow(() -> new OJDatabaseException("No result for bean id " + beanIdCondition.getValue() + " found. field: " + pField)));
  }

  @Override
  public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue, boolean pAllowNewField)
  {
    builder.doUpdate(pUpdate -> pUpdate
        .set(new _ColumnTuple<>(pField, columns.get(pField).id, pValue))
        .where(beanIdCondition)
        .update());
  }

  @Override
  public <TYPE> void removeField(IField<TYPE> pField)
  {
    throw new UnsupportedOperationException("It's not allowed to remove fields from a persistent bean!");
  }

  @NotNull
  @Override
  public Iterator<FieldTuple<?>> iterator()
  {
    List<FieldTuple<?>> fieldTuples = builder.doSelect(pSelect -> pSelect
        .where(beanIdCondition)
        .firstResult()
        .map(this::_mapResultToFieldTuples))
        .orElseThrow(() -> new OJDatabaseException("No result for bean id " + beanIdCondition.getValue() + " found."));
    return fieldTuples.iterator();
  }

  /**
   * Transfers the result of a select query to a list of bean tuples.
   *
   * @param pResultRow the result row of the select query
   * @return a list of field value tuples
   */
  private List<FieldTuple<?>> _mapResultToFieldTuples(ResultRow pResultRow)
  {
    //noinspection unchecked
    return columns.entrySet().stream()
        .map(pEntry -> ((IField) pEntry.getKey()).newTuple(pResultRow.hasColumn(pEntry.getValue()) ? pResultRow.get(pEntry.getValue()) : null))
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
        .collect(Collectors.toMap(pField -> pField, pField -> new _ColumnIdentification(pField, id.getAndIncrement())));
  }

  /**
   * Checks, if columns must be added.
   */
  private void _checkColumnSize()
  {
    IntStream.range(builder.getColumnCount() - 1, columns.size())
        .mapToObj(pIndex -> IDatabaseConstants.BEAN_TABLE_COLUMN_PREFIX + pIndex)
        .forEach(pColumnName -> builder.addColumn(IColumnDefinition.of(pColumnName, EColumnType.VARCHAR, 255)));
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
   * @param <TYPE> the data type of the associated bean field
   */
  private class _ColumnIdentification<TYPE> implements IColumnIdentification<TYPE>
  {
    private final IField<TYPE> beanField;
    private final int id;

    /**
     * Creates a new column identification.
     *
     * @param pBeanField the bean field associated with the column
     * @param pId        the index of the column
     */
    private _ColumnIdentification(IField<TYPE> pBeanField, int pId)
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
    public TYPE fromSerial(String pSerial)
    {
      return SQLSerializer.fromPersistent(beanField, pSerial);
    }
  }

  /**
   * A column value tuple for this special database table.
   * The column name is built from a prefix and the index of the column.
   *
   * @param <TYPE> the data type of the associated bean tuple
   */
  private class _ColumnTuple<TYPE> implements IColumnValueTuple<TYPE>
  {
    private final IField<TYPE> beanField;
    private final IColumnDefinition columnDefinition;
    private final TYPE value;

    /**
     * Creates a new column value tuple.
     *
     * @param pBeanField the associated bean field
     * @param pId        the index of the column
     * @param pValue     the data value for the column
     */
    private _ColumnTuple(IField<TYPE> pBeanField, int pId, TYPE pValue)
    {
      beanField = pBeanField;
      columnDefinition = IColumnDefinition.of(IDatabaseConstants.BEAN_TABLE_COLUMN_PREFIX + pId, EColumnType.VARCHAR, 255);
      value = pValue;
    }

    @Override
    public IColumnDefinition getColumnDefinition()
    {
      return columnDefinition;
    }

    @Override
    public TYPE getValue()
    {
      return value;
    }

    @Override
    public String toSerial()
    {
      return SQLSerializer.toPersistent(beanField.newTuple(getValue()));
    }
  }
}
