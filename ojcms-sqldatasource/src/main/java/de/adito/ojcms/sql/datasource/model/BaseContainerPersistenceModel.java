package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.sql.datasource.model.column.*;
import de.adito.ojcms.sql.datasource.util.OJSQLException;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.column.*;
import de.adito.ojcms.sqlbuilder.result.ResultRow;
import de.adito.ojcms.transactions.api.*;

import java.util.*;
import java.util.stream.Collectors;

import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.BEAN_TYPE_COLUMN_NAME;
import static java.util.Collections.singleton;
import static java.util.function.Function.identity;

/**
 * The persistence model for a database table for a persistent base bean container.
 * Provides meta information to initialize the database table.
 * Also handles the requesting of bean data and processing of changes.
 * Defines special handling for different bean sub types.
 *
 * @author Simon Danner, 18.03.2020
 */
public class BaseContainerPersistenceModel extends ContainerPersistenceModel
{
  private static final IColumnDefinition BEAN_TYPE_COLUMN = IColumnDefinition.of(BEAN_TYPE_COLUMN_NAME, EColumnType.STRING.create());
  private static final IColumnIdentification<String> BEAN_TYPE_COLUMN_ID = IColumnIdentification.of(BEAN_TYPE_COLUMN_NAME, String.class);

  private final Map<Class<? extends IBean>, Set<BeanColumnIdentification<?>>> columnsBySubType;

  /**
   * Initializes the persistence model for a persistent base bean container.
   *
   * @param pContainerId the id of the persistent container
   * @param pSubTypes    all supported sub bean types of the container
   */
  BaseContainerPersistenceModel(String pContainerId, Set<Class<? extends IBean>> pSubTypes)
  {
    super(pContainerId, _resolveAllColumns(pSubTypes), BEAN_TYPE_COLUMN_ID);
    columnsBySubType = pSubTypes.stream() //
        .collect(Collectors.toMap(identity(), BaseContainerPersistenceModel::columnIdentificationsFromBeanType));
  }

  /**
   * Loads a concrete bean type of a database entry by index.
   *
   * @param pKey     the index based key to identify the bean
   * @param pBuilder a builder to execute SQL statements
   * @return the type of the bean at the requested index
   */
  public <BEAN extends IBean> Class<BEAN> loadBeanType(InitialIndexKey pKey, OJSQLBuilder pBuilder)
  {
    //noinspection unchecked
    return (Class<BEAN>) selectSingleResultByIndex(pKey, singleton(BEAN_TYPE_COLUMN_ID), pBuilder,
        BaseContainerPersistenceModel::_resolveBeanType);
  }

  @Override
  protected List<IColumnDefinition> getColumnsToCreateInitially()
  {
    final List<IColumnDefinition> columnsToCreate = super.getColumnsToCreateInitially();
    columnsToCreate.add(BEAN_TYPE_COLUMN);
    return columnsToCreate;
  }

  @Override
  protected List<IColumnValueTuple<?>> tuplesToInsertForNewBean(BeanAddition pBeanAddition)
  {
    final List<IColumnValueTuple<?>> tuplesToInsert = super.tuplesToInsertForNewBean(pBeanAddition);
    tuplesToInsert.add(IColumnValueTuple.of(BEAN_TYPE_COLUMN_ID, pBeanAddition.getBeanType().getName()));
    return tuplesToInsert;
  }

  @Override
  protected Map<IField<?>, Object> resultRowToBeanContent(ResultRow pResultRow)
  {
    final Class<? extends IBean> beanType = _resolveBeanType(pResultRow);
    if (!columnsBySubType.containsKey(beanType))
      throw new IllegalStateException("Bean type " + beanType.getName() + " not registered for persistence model!");

    return pResultRow.toMap(columnsBySubType.get(beanType), BeanColumnIdentification::getBeanField);
  }

  /**
   * Resolves the bean type from a {@link ResultRow} from the bean type column.
   *
   * @param pResultRow the result row to obtain the bean type from
   * @return the resolved bean type
   */
  private static Class<? extends IBean> _resolveBeanType(ResultRow pResultRow)
  {
    try
    {
      //noinspection unchecked
      return (Class<? extends IBean>) Class.forName(pResultRow.get(BEAN_TYPE_COLUMN_ID));
    }
    catch (ClassNotFoundException pE)
    {
      throw new OJSQLException("Unable to resolve bean type!", pE);
    }
  }

  /**
   * Creates all {@link BeanColumnIdentification} definitions for a concrete bean type.
   *
   * @param pBeanType the bean type to resolve identifications for
   * @return the resolved bean based column identifications
   */
  private static Set<BeanColumnIdentification<?>> columnIdentificationsFromBeanType(Class<? extends IBean> pBeanType)
  {
    return BeanReflector.reflectBeanFields(pBeanType).stream() //
        .map(BeanColumnIdentification::new) //
        .collect(Collectors.toSet());
  }

  /**
   * Resolves all distinct {@link BeanColumnDefinition} for the supported sub types of the base container.
   *
   * @param pSubTypes all supported sub bean types
   * @return a set of distinct bean column definitions over all sub types
   */
  private static Set<BeanColumnDefinition<?>> _resolveAllColumns(Set<Class<? extends IBean>> pSubTypes)
  {
    return pSubTypes.stream() //
        .flatMap(pSubType -> BeanReflector.reflectBeanFields(pSubType).stream()) //
        .distinct() //
        .map(BeanColumnDefinition::new) //
        .collect(Collectors.toSet());
  }
}
