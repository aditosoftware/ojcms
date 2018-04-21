package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import de.adito.beans.core.util.beancopy.*;
import de.adito.beans.core.util.exceptions.BeanCopyUnsupportedException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds any generic type value.
 *
 * @param <TYPE> the generic data type this field is referring to
 * @author Simon Danner, 07.09.2017
 */
public class GenericField<TYPE> extends AbstractField<TYPE>
{
  public GenericField(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(_checkGenericType(pType), pName, pAnnotations);
  }

  @Override
  public TYPE copyValue(TYPE pValue, CustomFieldCopy<?>... pCustomFieldCopies) throws BeanCopyUnsupportedException
  {
    try
    {
      return BeanCopyUtil.tryCopyPerDefaultConstructor(pValue);
    }
    catch (UnsupportedOperationException pE)
    {
      throw new BeanCopyUnsupportedException(this);
    }
  }

  /**
   * Checks, if this generic field may be replaced by a bean- or bean container field.
   *
   * @param pGenericType the generic type of this field
   * @return the generic type to use in a super call
   */
  protected static <TYPE> Class<TYPE> _checkGenericType(Class<TYPE> pGenericType)
  {
    if (IBean.class.isAssignableFrom(pGenericType))
      _throwPossibleReplacementError(pGenericType, BeanField.class);
    if (IBeanContainer.class.isAssignableFrom(pGenericType))
      _throwPossibleReplacementError(pGenericType, ContainerField.class);
    return pGenericType;
  }

  /**
   * Throws a runtime exception that indicates this field can be replaced by another bean field.
   *
   * @param pGenericType     the generic type of this field
   * @param pReplacementType the type of the replacement field
   */
  private static <TYPE> void _throwPossibleReplacementError(Class<TYPE> pGenericType, Class<? extends IField> pReplacementType)
  {
    throw new RuntimeException("A generic field is not required for this type. Use a " + pReplacementType.getSimpleName() + " instead. " +
                                   "Generic-Type: " + pGenericType.getSimpleName());
  }
}
