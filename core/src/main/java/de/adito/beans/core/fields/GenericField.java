package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IBeanContainer;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Ein Bean-Feld für einen beliebigen generischen Typen.
 *
 * @author s.danner, 07.09.2017
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
