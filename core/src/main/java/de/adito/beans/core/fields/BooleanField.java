package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.fields.serialization.*;
import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a boolean value.
 *
 * @author Simon Danner, 19.01.2017
 */
@TypeDefaultField(types = Boolean.class)
public class BooleanField extends AbstractField<Boolean> implements ISerializableFieldToString<Boolean>
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
