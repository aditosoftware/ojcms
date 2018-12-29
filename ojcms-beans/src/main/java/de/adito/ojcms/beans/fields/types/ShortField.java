package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.fields.serialization.ISerializableFieldToString;
import de.adito.ojcms.beans.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.*;
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
public class ShortField extends AbstractField<Short> implements ISerializableFieldToString<Short>
{
  protected ShortField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Short.class, pName, pAnnotations);
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

  @Override
  public Short fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Short.parseShort(pSerialString);
  }
}
