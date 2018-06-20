package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.mappers.IBeanFlatDataMapper;
import de.adito.beans.core.util.IBeanFieldPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A container for base data of a bean encapsulated data core.
 * It kind of replaces the necessity of an abstract class.
 *
 * @param <BEAN>     the type of the beans in the core
 * @param <LISTENER> the type of the bean listeners managed here
 * @author Simon Danner, 16.03.2018
 */
class BeanEncapsulatedContainers<BEAN extends IBean<BEAN>, LISTENER extends IBeanChangeListener<BEAN>>
    extends EncapsulatedContainers<BEAN, LISTENER>
{
  private final List<IBeanFieldPredicate> fieldFilters = new ArrayList<>();
  private final List<BeanDataMapper> dataMappers = new ArrayList<>();

  /**
   * A container for field filters for this bean data core.
   */
  public List<IBeanFieldPredicate> getFieldFilters()
  {
    return fieldFilters;
  }

  /**
   * A container for data mappers for this bean data core.
   */
  public List<BeanDataMapper> getDataMappers()
  {
    return dataMappers;
  }

  /**
   * Wrapper for a bean data mapper and an optional bean field it should apply to.
   */
  static class BeanDataMapper
  {
    private final IBeanFlatDataMapper dataMapper;
    @Nullable
    private final IField<?> beanField;

    public BeanDataMapper(IBeanFlatDataMapper pDataMapper)
    {
      this(pDataMapper, null);
    }

    public BeanDataMapper(IBeanFlatDataMapper pDataMapper, @Nullable IField<?> pBeanField)
    {
      dataMapper = pDataMapper;
      beanField = pBeanField;
    }

    /**
     * The actual data mapper.
     */
    public IBeanFlatDataMapper getDataMapper()
    {
      return dataMapper;
    }

    /**
     * An optional bean field, which the mapper should apply to only
     */
    @Nullable
    public IField<?> getBeanField()
    {
      return beanField;
    }
  }
}
