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
 * A bean field that holds a short.
 *
 * @author Simon Danner, 30.07.2018
 */
@NeverNull
@TypeDefaultField(types = Short.class)
public class ShortField extends AbstractField<Short> implements IAutoSerializableField<Short>
{
  protected ShortField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(Short.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Short getInitialValue()
  {
    return 0;
  }

  @Override
  public Short copyValue(Short pShort, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pShort;
  }
}
