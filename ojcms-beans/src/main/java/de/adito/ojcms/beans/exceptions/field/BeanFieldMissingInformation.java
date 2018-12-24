package de.adito.ojcms.beans.exceptions.field;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.fields.util.IAdditionalFieldInfo;

import java.lang.annotation.Annotation;

/**
 * General exception for bean fields missing a certain information that is expected to be present.
 *
 * @author Simon Danner, 23.12.2018
 */
public class BeanFieldMissingInformation extends OJRuntimeException
{
  /**
   * Creates a new missing information exception for a certain annotation type.
   *
   * @param pMissingAnnotationType the missing annotation type
   */
  public BeanFieldMissingInformation(Class<? extends Annotation> pMissingAnnotationType)
  {
    super("Annotation " + pMissingAnnotationType.getName() + " is not present at this field!");
  }

  /**
   * Creates a new missing information exception for a certain {@link IAdditionalFieldInfo}.
   *
   * @param pFieldInfoIdentifier the missing field information identifier
   */
  public BeanFieldMissingInformation(IAdditionalFieldInfo<?> pFieldInfoIdentifier)
  {
    super("Additional information of type " + pFieldInfoIdentifier.getDataType().getName() + " is not present at this field!");
  }
}
