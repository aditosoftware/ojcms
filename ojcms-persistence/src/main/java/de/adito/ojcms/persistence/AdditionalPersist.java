package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;

import java.lang.annotation.*;

/**
 * Allows to register an additional persistent bean.
 *
 * @author Simon Danner, 09.05.2020
 * @see AdditionalPersistConfiguration
 */
@Repeatable(AdditionalPersist.Multiple.class)
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalPersist
{
  /**
   * The type of the persistent bean.
   */
  Class<? extends IBean> beanType();

  /**
   * The id of the container in that the bean/beans are stored.
   *
   * @return the container id
   */
  String containerId();

  /**
   * The mode in which the beans should be persisted.
   * Default: they will be stored in a container.
   *
   * @return the persistence mode to use
   */
  EPersistenceMode mode() default EPersistenceMode.CONTAINER;

  /**
   * Required to repeat this annotation.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Multiple
  {
    AdditionalPersist[] value();
  }
}
