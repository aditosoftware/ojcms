package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.IAutoSerializableField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a boolean value.
 *
 * @author Simon Danner, 19.01.2017
 */
@NeverNull
@TypeDefaultField(types = Boolean.class)
public class BooleanField extends AbstractField<Boolean> implements IAutoSerializableField<Boolean>
{
  protected BooleanField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(Boolean.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Boolean getInitialValue()
  {
    return false;
  }

  @Override
  public Boolean copyValue(Boolean pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }
}
