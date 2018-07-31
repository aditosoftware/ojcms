package de.adito.beans.persistence;

import de.adito.beans.core.IBean;

/**
 * General utility class for the persistence framework.
 *
 * @author Simon Danner, 26.02.2018
 */
public final class BeanPersistenceUtil
{
  private BeanPersistenceUtil()
  {
  }

  /**
   * Instantiates a new bean from a certain bean type.
   * The bean type has to annotated with {@link Persist}, otherwise a runtime exception will be thrown.
   *
   * @param pBeanType the bean type to instantiate
   * @param <BEAN>    the generic bean type
   * @return the create bean instance.
   */
  public static <BEAN extends IBean<BEAN>> BEAN newInstance(Class<BEAN> pBeanType)
  {
    try
    {
      if (!pBeanType.isAnnotationPresent(Persist.class))
        throw new RuntimeException("The bean type '" + pBeanType.getName() + "' is not marked as persistent bean!");
      return pBeanType.newInstance();
    }
    catch (InstantiationException | IllegalAccessException pE)
    {
      throw new RuntimeException("The persistent bean type '" + pBeanType.getName() + "' must define a default constructor!");
    }
  }
}
