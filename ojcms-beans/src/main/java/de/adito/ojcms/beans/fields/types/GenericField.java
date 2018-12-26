package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.annotations.GenericBeanField;
import de.adito.ojcms.beans.exceptions.copy.BeanCopyNotSupportedException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A bean field that holds any generic type value.
 *
 * @param <TYPE> the generic data type this field is referring to
 * @author Simon Danner, 07.09.2017
 */
@GenericBeanField
public class GenericField<TYPE> extends AbstractField<TYPE>
{
  //open for testing purposes
  static Logger LOGGER = Logger.getLogger(GenericField.class.getName()); //NOSONAR

  protected GenericField(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(_checkGenericType(pType), pName, pAnnotations);
  }

  @Override
  public TYPE copyValue(TYPE pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies) throws BeanCopyNotSupportedException
  {
    try
    {
      //noinspection unchecked
      return (TYPE) pValue.getClass().newInstance();
    }
    catch (IllegalAccessException | InstantiationException pE)
    {
      throw new BeanCopyNotSupportedException(this, pE);
    }
  }

  /**
   * Checks, if this generic field may be replaced by an existing concrete bean field type.
   *
   * @param pGenericType the generic type of this field
   * @return the generic type to use in a super call
   */
  private static <TYPE> Class<TYPE> _checkGenericType(Class<TYPE> pGenericType)
  {
    IField.findFieldTypeFromDataType(pGenericType)
        .ifPresent(pReplacement ->
                       LOGGER.warning("A generic field is not required for this data type. Use " + pReplacement.getName() + " instead." +
                                          " generic type: " + pGenericType.getName()));
    return pGenericType;
  }
}
