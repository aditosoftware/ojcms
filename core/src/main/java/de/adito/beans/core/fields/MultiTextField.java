package de.adito.beans.core.fields;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a multiline text.
 *
 * @author Simon Danner, 17.01.2017
 */
public class MultiTextField extends TextField
{
  public MultiTextField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pName, pAnnotations);
  }
}
