package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.persistence.*;

import java.lang.reflect.*;

import static de.adito.ojcms.persistence.datasource.BeanProducerExtension.BASE_CONTAINER_TYPES;

/**
 * Utility class to create {@link IBean} instances with persistent data sources.
 *
 * @author Simon Danner, 31.12.2019
 */
final class BeanPersistenceUtil
{
  private BeanPersistenceUtil()
  {
  }

  /**
   * Instantiates a new bean for a given bean type and configures a persistent {@link IBeanDataSource} for it.
   * The bean type has to annotated with {@link Persist} or be a sub type of bean annotated by {@link PersistAsBaseType}, otherwise a
   * runtime exception will be thrown. The bean type has to define a default constructor.
   *
   * @param pBeanType   the bean type to instantiate
   * @param pDataSource the persistent data source to use for the bean instance
   * @param <BEAN>      the generic bean type
   * @return the created bean instance
   * @throws OJPersistenceException if the persistence annotation is missing
   */
  static <BEAN extends IBean> BEAN newPersistentBeanInstance(Class<BEAN> pBeanType, IBeanDataSource pDataSource)
  {
    try
    {
      if (!pBeanType.isAnnotationPresent(Persist.class) && !_isSubTypeOfBaseContainer(pBeanType))
        throw new OJPersistenceException(pBeanType);

      final Constructor<BEAN> defaultConstructor = pBeanType.getDeclaredConstructor();
      if (!defaultConstructor.isAccessible())
        defaultConstructor.setAccessible(true);

      final BEAN bean = defaultConstructor.newInstance();
      bean.setEncapsulatedDataSource(pDataSource);
      return bean;
    }
    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException pE)
    {
      throw new OJPersistenceException(
          "The persistent bean type '" + pBeanType.getName() + "' must define a default constructor" + " (can be private)!", pE);
    }
  }

  /**
   * Determines if some bean type is a sub type of a registered base bean type for a base container.
   *
   * @param pPotentialSubType the sub type to check
   * @return <tt>true</tt> if the bean type is a registered sub type of a base container
   */
  private static boolean _isSubTypeOfBaseContainer(Class<? extends IBean> pPotentialSubType)
  {
    return BASE_CONTAINER_TYPES.entrySet().stream() //
        .filter(pEntry -> pEntry.getKey().isAssignableFrom(pPotentialSubType)) //
        .anyMatch(pEntry -> pEntry.getValue().getSubTypes().contains(pPotentialSubType));
  }
}
