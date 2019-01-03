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
 * A bean field that holds a boolean value.
 *
 * @author Simon Danner, 19.01.2017
 */
@NeverNull
@TypeDefaultField(types = Boolean.class)
public class BooleanField extends AbstractField<Boolean> implements ISerializableFieldToString<Boolean>
{
  protected BooleanField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional)
  {
    super(Boolean.class, pName, pAnnotations, pIsOptional);
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

  @Override
  public Boolean fromPersistent(String pSerialString)
  {
    return Boolean.parseBoolean(pSerialString);
  }
}
