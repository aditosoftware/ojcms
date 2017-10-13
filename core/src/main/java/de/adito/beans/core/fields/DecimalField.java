package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Beschreibt ein Bean-Feld, welches eine Gleitkommazahl beinhaltet.
 *
 * @author s.danner, 23.08.2016
 */
@TypeDefaultField(types = Double.class)
public class DecimalField extends AbstractField<Double>
{
  public DecimalField(@NotNull Class<Double> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public Double getDefaultValue()
  {
    return 0.0;
  }
}
