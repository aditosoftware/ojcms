package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Beschreibt ein Long-Bean-Feld.
 *
 * @author s.danner, 14.02.2017
 */
@TypeDefaultField(types = Long.class)
public class LongField extends AbstractField<Long>
{
  public LongField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Long.class, pName, pAnnotations);
  }

  @Override
  public Long getDefaultValue()
  {
    return 0L;
  }
}
