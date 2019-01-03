package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.annotations.GenericBeanField;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.fields.serialization.ISerializableField;
import de.adito.ojcms.beans.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * A bean field for an enumeration type.
 *
 * @author Simon Danner, 01.08.2018
 */
@TypeDefaultField(types = Enum.class)
@GenericBeanField
public class EnumField<ENUM extends Enum> extends AbstractField<ENUM> implements ISerializableField<ENUM>
{
  protected EnumField(Class<ENUM> pType, @NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional)
  {
    super(pType, pName, pAnnotations, pIsOptional);
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
      final Method valueOf = getDataType().getMethod("valueOf", String.class);
      if (!valueOf.isAccessible())
        valueOf.setAccessible(true);
      //noinspection unchecked
      return (ENUM) valueOf.invoke(null, pSerialString);
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException pE)
    {
      throw new OJInternalException("Unable to convert an enum field's persistent value! value: " + pSerialString, pE);
    }
  }

  @Override
  public ENUM copyValue(ENUM pEnum, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pEnum;
  }
}
