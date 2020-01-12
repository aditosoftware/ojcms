package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.sql.datasource.model.column.*;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.sqlbuilder.result.ResultRow;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.exceptions.BeanDataNotFoundException;
import de.adito.ojcms.utils.StringUtility;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * The persistence model for a database table for a persistent bean container.
 * Provides meta information to initialize the database table and to load requested data by queries.
 *
 * @author Simon Danner, 01.01.2020
 */
public class ContainerPersistenceModel implements IPersistenceModel<IContainerBeanKey>
{
  private final String containerId;
  private final BeanColumnDefinition[] columnDefinitions;
  private final BeanColumnIdentification<?>[] columns;

  /**
   * Initializes the persistence model for a persistent bean container.
   *
   * @param pContainerId the container id of the persistent container
   * @param pBeanType    the type of beans within the bean container
   */
  ContainerPersistenceModel(String pContainerId, Class<? extends IBean<?>> pBeanType)
  {
    containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
    final List<IField<?>> beanFields = BeanReflector.reflectBeanFields(pBeanType);
    columnDefinitions = BeanColumnDefinition.ofFields(beanFields);
    columns = BeanColumnIdentification.ofMultiple(beanFields);
  }

  @Override
  public void initModelInDatabase(OJSQLBuilder pBuilder)
  {
    pBuilder.ifTableNotExistingCreate(containerId, pCreate -> pCreate
        .withIdColumn()
        .columns(columnDefinitions)
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

  @Override
  public PersistentBeanData loadDataByKey(IContainerBeanKey pKey, OJSQLBuilder pBuilder)
  {
    if (pKey instanceof BeanIndexKey)
      return _loadByIndex((BeanIndexKey) pKey, pBuilder);
    else if (pKey instanceof BeanIdentifiersKey)
      return _loadByIdentifiers((BeanIdentifiersKey) pKey, pBuilder);
    else
      throw new UnsupportedOperationException("Container key of type " + pKey.getClass().getName() + " not supported!");
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
        .select(columns)
        .withId()
        .from(containerId)
        .fullResult()
        .stream()
        .map(this::_toBeanData)
        .collect(Collectors.toMap(PersistentBeanData::getIndex, identity())));

  }

  /**
   * Loads {@link PersistentBeanData} from the container by a {@link BeanIndexKey}.
   * Throws a {@link BeanDataNotFoundException} if there's no data at the given index.
   *
   * @param pKey     the index based bean key
   * @param pBuilder a builder to execute SQL statements
   * @return the requested bean data
   */
  private PersistentBeanData _loadByIndex(BeanIndexKey pKey, OJSQLBuilder pBuilder)
  {
    final Map<IField<?>, Object> beanContent = pBuilder.doSelect(pSelect -> pSelect
        .select(columns)
        .from(containerId)
        .whereId(pKey.getIndex())
        .firstResult()
        .orElseThrow(() -> new BeanDataNotFoundException(pKey))
        .toMap(columns, BeanColumnIdentification::getBeanField));

    return new PersistentBeanData(pKey.getIndex(), beanContent);
  }

  /**
   * Loads {@link PersistentBeanData} from the container by a {@link BeanIdentifiersKey}.
   * Throws a {@link BeanDataNotFoundException} if there's no data for the given identifiers.
   *
   * @param pKey     the identifier based bean key
   * @param pBuilder a builder to execute SQL statements
   * @return the requested bean data
   */
  private PersistentBeanData _loadByIdentifiers(BeanIdentifiersKey pKey, OJSQLBuilder pBuilder)
  {
    final ResultRow resultRow = pBuilder.doSelect(pSelect -> pSelect
        .select(columns)
        .withId()
        .from(containerId)
        .where(BeanWhereCondition.ofMap(pKey.getIdentifiers()))
        .firstResult()
        .orElseThrow(() -> new BeanDataNotFoundException(pKey)));

    return _toBeanData(resultRow);
  }

  /**
   * Converts a SQL {@link ResultRow} to {@link PersistentBeanData}.
   *
   * @param pResultRow the SQL result row to convert
   * @return the converted persistent bean data
   */
  private PersistentBeanData _toBeanData(ResultRow pResultRow)
  {
    final Map<IField<?>, Object> beanContent = pResultRow.toMap(columns, BeanColumnIdentification::getBeanField);
    return new PersistentBeanData(pResultRow.getId(), beanContent);
  }
}
