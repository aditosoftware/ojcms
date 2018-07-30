package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds an Integer.
 *
 * @author Simon Danner, 27.01.2017
 */
@TypeDefaultField(types = Integer.class)
public class IntegerField extends AbstractField<Integer> implements ISerializableField<Integer>
{
  public IntegerField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Integer.class, pName, pAnnotations);
  }

  @Override
  public Integer getDefaultValue()
  {
    return 0;
  }

  @Override
  public Integer getInitialValue()
  {
    return 0;
  }

  @Override
  public Integer copyValue(Integer pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }

  @Override
  public Integer fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Integer.parseInt(pSerialString);
  }
}
