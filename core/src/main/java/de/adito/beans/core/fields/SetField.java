package de.adito.beans.core.fields;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link Set}.
 *
 * @author Simon Danner, 01.08.2018
 */
public class SetField<TYPE> extends AbstractField<Set<TYPE>>
{
  public SetField(@NotNull Class<Set<TYPE>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }
}
