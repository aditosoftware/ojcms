package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that hold a boolean value.
 *
 * @author Simon Danner, 19.01.2017
 */
@TypeDefaultField(types = Boolean.class)
public class BooleanField extends AbstractField<Boolean>
{
  public BooleanField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Boolean.class, pName, pAnnotations);
  }

  @Override
  public Boolean getDefaultValue()
  {
    return false;
  }
}
