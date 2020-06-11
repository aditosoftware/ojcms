package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Use this annotation for bean fields to define their declaration order. This is necessary if you are using a JVM implementation that
 * does not provide fields in their declaration order naturally (via {@link Class#getDeclaredFields()}.
 * <p>
 * If this annotation is used at least for one bean field, all other fields must be annotated as well. Otherwise a runtime exception will
 * be thrown. The order numbers for a bean class must not contain duplicates, but may have gaps (like 100, 200, 300, ...).
 * <p>
 * Lower numbers are declared before higher numbers.
 *
 * @author Simon Danner, 11.06.2020
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldOrder
{
  /**
   * A number indicating the declaration position of the annotated bean field (lower number = lower position)
   */
  int value();
}
