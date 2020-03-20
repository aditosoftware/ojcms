package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;

import java.lang.annotation.*;

/**
 * A persistent bean container for beans annotated by this annotation will be created as base type container for multiple sub types.
 * The base bean type itself can also be used if it is not abstract.
 *
 * @author Simon Danner, 10.02.2018
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PersistAsBaseType
{
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
}
