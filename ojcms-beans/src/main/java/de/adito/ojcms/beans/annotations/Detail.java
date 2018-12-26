package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean field as detail.
 * The application determines how to use this information.
 *
 * @author Simon Danner, 23.08.2016
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detail
{
  Detail INSTANCE = new Detail() { //NOSONAR
    @Override
    public Class<? extends Annotation> annotationType()
    {
      return Detail.class;
    }
  };
}
