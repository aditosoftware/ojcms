package de.adito.beans.persistence;

import de.adito.beans.core.annotations.ObserveCreation;

import java.lang.annotation.*;

/**
 * Persistence annotation of the framework.
 * A bean annotated by this type will be persisted according to a certain {@link EPersistenceMode}.
 * Per default all beans of the annotated bean type will be stored in a persistent bean container.
 * Also per default all beans created (anywhere in the code; per constructor) will be stored in the belonging container automatically.
 *
 * @author Simon Danner, 10.02.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ObserveCreation
public @interface Persist
{
  /**
   * The container id of the element, in which the bean/beans are stored.
   */
  String containerId();

  /**
   * The mode, in which the beans should be persisted.
   */
  EPersistenceMode mode() default EPersistenceMode.CONTAINER;
}
