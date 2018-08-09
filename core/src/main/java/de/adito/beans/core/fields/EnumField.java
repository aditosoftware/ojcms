package de.adito.beans.core.fields;

import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;

/**
 * A bean field for an enumeration type.
 *
 * @author Simon Danner, 01.08.2018
 */
public class EnumField<ENUM extends Enum> extends AbstractField<ENUM> implements ISerializableField<ENUM>
{
  public EnumField(@NotNull Class<ENUM> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public String toPersistent(ENUM pValue)
  {
    return pValue.name();
  }

  @Override
  public ENUM fromPersistent(String pSerialString)
  {
    try
    {
      final Method valueOf = getType().getMethod("valueOf", String.class);
      if (!valueOf.isAccessible())
        valueOf.setAccessible(true);
      //noinspection unchecked
      return (ENUM) valueOf.invoke(null, pSerialString);
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException pE)
    {
      throw new RuntimeException("Unable to convert an enum field's persistent value! value: " + pSerialString, pE);
    }
  }

  @Override
  public ENUM copyValue(ENUM pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }
}
