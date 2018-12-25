package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Marks generic bean fields. Used for internal purposes for initialization.
 * Every bean field using a generic type in its data type has to use this annotation.
 *
 * @author Simon Danner, 25.12.2018
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericBeanField
{
  /**
   * An optional wrapper type for the actual generic type of the bean field.
   * Might be {@link de.adito.ojcms.beans.IBeanContainer} for container fields where the bean type is the generic type of the field.
   *
   * @return a generic wrapper type for the actual generic type, or 'void.class' for none
   */
  Class<?> genericWrapperType() default void.class;
}
