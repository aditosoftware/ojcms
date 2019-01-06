package de.adito.ojcms.beans.literals;

import de.adito.ojcms.beans.exceptions.field.BeanFieldMissingInformation;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A static member literal of a bean.
 * A literal is used to store additional information about the member to avoid reflection.
 * Furthermore the application is able to refer to members statically.
 * This may be very useful in situations where highly abstract structures are used (Java doesn't support method literals, for example).
 *
 * @author Simon Danner, 06.01.2019
 */
public interface IMemberLiteral
{
  /**
   * The declared name of this member.
   *
   * @return the name of the member
   */
  String getName();

  /**
   * An optional annotation instance of this member for a certain annotation type.
   * The annotation may not be present.
   *
   * @param pType the annotation's type
   * @return an optional annotation instance for a certain type
   */
  <ANNOTATION extends Annotation> Optional<ANNOTATION> getAnnotation(Class<ANNOTATION> pType);

  /**
   * An annotation instance of this member for a certain annotation type.
   * If the annotation is not present, this will lead to a runtime exception.
   *
   * @param pType the annotation's type
   * @return an annotation instance for a certain type
   * @throws BeanFieldMissingInformation if the requested annotation is not present
   */
  default <ANNOTATION extends Annotation> ANNOTATION getAnnotationOrThrow(Class<ANNOTATION> pType)
  {
    return getAnnotation(pType).orElseThrow(() -> new BeanFieldMissingInformation(pType));
  }

  /**
   * Determines, if this member is annotated by a specific annotation type.
   *
   * @param pType the annotation's type
   * @return <tt>true</tt>, if the annotation is present
   */
  boolean hasAnnotation(Class<? extends Annotation> pType);

  /**
   * All annotations of this member.
   *
   * @return a collection of annotations
   */
  Collection<Annotation> getAnnotations();

  /**
   * An optional additional information for this member.
   * The information may not have been added.
   *
   * @param pIdentifier the identifier of the information
   * @return the optional additional information
   */
  <INFO> Optional<INFO> getAdditionalInformation(IAdditionalMemberInfo<INFO> pIdentifier);

  /**
   * An optional additional information for this member.
   * The information may not have been added.
   *
   * @param pIdentifier the identifier of the information
   * @return the optional additional information
   */
  default <INFO> INFO getAdditionalInformationOrThrow(IAdditionalMemberInfo<INFO> pIdentifier)
  {
    return getAdditionalInformation(pIdentifier).orElseThrow(() -> new BeanFieldMissingInformation(pIdentifier));
  }

  /**
   * Adds any additional information to this member.
   *
   * @param pIdentifier the identifier of the information
   * @param pValue      the information
   */
  <INFO> void addAdditionalInformation(IAdditionalMemberInfo<INFO> pIdentifier, INFO pValue);

  /**
   * Determines, if this member has private access only.
   *
   * @return <tt>true</tt>, if the access modifier private is set
   */
  boolean isPrivate();
}
