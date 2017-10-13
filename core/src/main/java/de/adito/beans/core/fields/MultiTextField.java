package de.adito.beans.core.fields;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Beschreibt ein spezielles Bean-Feld für einen mehrzeiligen Text.
 *
 * @author s.danner, 17.01.2017
 */
public class MultiTextField extends TextField
{
  public MultiTextField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pName, pAnnotations);
  }
}
