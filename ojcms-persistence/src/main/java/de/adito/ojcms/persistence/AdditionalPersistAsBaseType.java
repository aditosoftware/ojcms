package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;

import java.lang.annotation.*;

/**
 * Allows to register an additional persistent bean for some sub types.
 *
 * @author Simon Danner, 09.05.2020
 * @see AdditionalPersistConfiguration
 */
@Repeatable(AdditionalPersistAsBaseType.Multiple.class)
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdditionalPersistAsBaseType
{
  /**
   * The base type of the persistent bean.
   */
  Class<? extends IBean> baseType();

  /**
   * The container id of the element, in which the bean/beans are stored.
   *
   * @return the container id
   */
  String containerId();

  /**
   * All sub types the base type container is for. The base type itself can be included if it is not abstract.
   * At least two types must be provided. The given types must be assignable from the base bean type.
   *
   * @return an array of bean sub types the base container should be for
   */
  Class<? extends IBean>[] forSubTypes();

  /**
   * Required to repeat this annotation.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @interface Multiple
  {
    AdditionalPersistAsBaseType[] value();
  }
}
