package de.adito.beans.core;

import de.adito.beans.core.fields.util.FieldTuple;
import de.adito.beans.core.mappers.*;
import de.adito.beans.core.util.IBeanFieldPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of a bean encapsulated data core.
 * Handles field filters and data mappers.
 *
 * @author Simon Danner, 24.11.2018
 */
abstract class AbstractBeanEncapsulated extends AbstractEncapsulated<FieldTuple<?>> implements IBeanEncapsulated
{
  private final Set<IBeanFieldPredicate> fieldFilters = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<BeanDataMapper> dataMappers = Collections.newSetFromMap(new ConcurrentHashMap<>());

  @Override
  public void addFieldFilter(IBeanFieldPredicate pPredicate)
  {
    fieldFilters.add(pPredicate);
  }

  @Override
  public void removeFieldFilter(IBeanFieldPredicate pPredicate)
  {
    fieldFilters.remove(pPredicate);
  }

  @Override
  public void clearFieldFilters()
  {
    fieldFilters.clear();
  }

  @Override
  public void addDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    dataMappers.add(new BeanDataMapper(pDataMapper));
  }

  @Override
  public <TYPE> void addDataMapperForField(IField<TYPE> pField, ISingleFieldFlatDataMapper<TYPE> pDataMapper)
  {
    dataMappers.add(new BeanDataMapper(pDataMapper, pField));
  }

  @Override
  public boolean removeDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    return dataMappers.removeIf(pMapper -> pMapper.getDataMapper() == pDataMapper);
  }

  @Override
  public void clearDataMappers()
  {
    dataMappers.clear();
  }

  /**
   * All field filters of this data core.
   *
   * @return a set of field filters
   */
  protected Set<IBeanFieldPredicate> getFieldFilters()
  {
    return Collections.unmodifiableSet(fieldFilters);
  }

  /**
   * All data mappers of this data core.
   *
   * @return a set of data mappers
   */
  protected Set<BeanDataMapper> getDataMappers()
  {
    return Collections.unmodifiableSet(dataMappers);
  }

  /**
   * Wrapper for a bean data mapper and an optional bean field it should apply to.
   */
  static class BeanDataMapper
  {
    private final IBeanFlatDataMapper dataMapper;
    private final Optional<IField<?>> beanField;

    public BeanDataMapper(IBeanFlatDataMapper pDataMapper)
    {
      this(pDataMapper, null);
    }

    public BeanDataMapper(IBeanFlatDataMapper pDataMapper, @Nullable IField<?> pBeanField)
    {
      dataMapper = pDataMapper;
      beanField = Optional.ofNullable(pBeanField);
    }

    /**
     * The actual data mapper.
     *
     * @return the data mapper
     */
    public IBeanFlatDataMapper getDataMapper()
    {
      return dataMapper;
    }

    /**
     * An optional bean field, which the mapper should apply to only.
     *
     * @return the optional bean field
     */
    public Optional<IField<?>> getBeanField()
    {
      return beanField;
    }
  }
}
