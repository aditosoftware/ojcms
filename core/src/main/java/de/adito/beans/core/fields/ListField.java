package de.adito.beans.core.fields;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link List}.
 *
 * @author Simon Danner, 01.08.2018
 */
public class ListField<TYPE> extends AbstractField<List<TYPE>>
{
  public ListField(@NotNull Class<List<TYPE>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }
}
