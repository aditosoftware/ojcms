package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a short.
 *
 * @author Simon Danner, 30.07.2018
 */
@TypeDefaultField(types = Short.class)
public class ShortField extends AbstractField<Short> implements ISerializableField<Short>
{
  public ShortField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Short.class, pName, pAnnotations);
  }

  @Override
  public Short getDefaultValue()
  {
    return 0;
  }

  @Override
  public Short getInitialValue()
  {
    return 0;
  }

  @Override
  public Short copyValue(Short pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }

  @Override
  public Short fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Short.parseShort(pSerialString);
  }
}
