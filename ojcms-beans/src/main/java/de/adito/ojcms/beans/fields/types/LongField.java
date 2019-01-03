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
 * A bean field that holds a long.
 *
 * @author Simon Danner, 14.02.2017
 */
@NeverNull
@TypeDefaultField(types = Long.class)
public class LongField extends AbstractField<Long> implements ISerializableFieldToString<Long>
{
  public LongField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional)
  {
    super(Long.class, pName, pAnnotations, pIsOptional);
  }

  @Override
  public Long getInitialValue()
  {
    return 0L;
  }

  @Override
  public Long copyValue(Long pLong, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pLong;
  }

  @Override
  public Long fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Long.parseLong(pSerialString);
  }
}
