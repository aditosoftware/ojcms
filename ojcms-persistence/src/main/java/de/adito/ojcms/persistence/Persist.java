package de.adito.ojcms.persistence;

import java.lang.annotation.*;

/**
 * A bean annotated by this annotation will be persisted according to a specific {@link EPersistenceMode}.
 * Per default, all beans of the annotated bean type will be stored in a persistent bean container.
 *
 * @author Simon Danner, 10.02.2018
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Persist
{
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
}
