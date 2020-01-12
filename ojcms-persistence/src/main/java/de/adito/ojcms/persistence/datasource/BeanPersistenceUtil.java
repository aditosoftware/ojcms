package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.persistence.*;

import java.lang.reflect.*;

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
   * The bean type has to annotated with {@link Persist}, otherwise a runtime exception will be thrown.
   * Also the bean type has to define a default constructor.
   *
   * @param pBeanType   the bean type to instantiate
   * @param pDataSource the persistent data source to use for the bean instance
   * @param <BEAN>      the generic bean type
   * @return the created bean instance
   * @throws OJPersistenceException if the persistence annotation is missing
   */
  static <BEAN extends IBean<BEAN>> BEAN newPersistentBeanInstance(Class<BEAN> pBeanType, IBeanDataSource pDataSource)
  {
    try
    {
      if (!pBeanType.isAnnotationPresent(Persist.class))
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
      throw new OJPersistenceException("The persistent bean type '" + pBeanType.getName() + "' must define a default constructor" +
                                           " (can be private)!", pE);
    }
  }
}
