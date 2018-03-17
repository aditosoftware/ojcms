package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.util.IBeanFieldPredicate;

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

  /**
   * A container for field filters for this bean data core.
   */
  public List<IBeanFieldPredicate> getFieldFilters()
  {
    return fieldFilters;
  }
}
