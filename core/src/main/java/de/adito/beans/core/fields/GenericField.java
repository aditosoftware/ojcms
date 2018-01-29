package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds any generic type value.
 *
 * @param <TYPE> the generic data type this field is referring to
 * @author Simon Danner, 07.09.2017
 */
public class GenericField<TYPE> extends AbstractField<TYPE>
{
  public GenericField(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
    if (IBean.class.isAssignableFrom(pType))
      throw new RuntimeException("type: " + pType.getSimpleName());
    if (IBeanContainer.class.isAssignableFrom(pType))
      throw new RuntimeException("type: " + pType.getSimpleName());
  }
}
