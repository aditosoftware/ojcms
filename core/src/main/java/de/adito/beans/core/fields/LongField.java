package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a long.
 *
 * @author Simon Danner, 14.02.2017
 */
@TypeDefaultField(types = Long.class)
public class LongField extends AbstractField<Long> implements ISerializableField<Long>
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

  @Override
  public Long getInitialValue()
  {
    return 0L;
  }

  @Override
  public Long copyValue(Long pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }

  @Override
  public Long fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Long.parseLong(pSerialString);
  }
}
