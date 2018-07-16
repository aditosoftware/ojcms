package de.adito.beans.core.annotations;

import de.adito.picoservice.PicoService;

import java.lang.annotation.*;

/**
 * This annotation defines a bean field type as default-field for a certain data type.
 * The mapping between data type and field type will be established at runtime.
 * The field marked with this annotation will be registered by the PicoService framework.
 *
 * @author Simon Danner, 29.06.2017
 * @see PicoService
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PicoService
public @interface TypeDefaultField
{
  /**
   * A collection of data types, which are the default types for the bean field marked by this annotation.
   *
   * @return the default data types for a bean field type
   */
  Class[] types();
}
