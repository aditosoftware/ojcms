package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.*;

import java.lang.annotation.Annotation;

/**
 * A bean has been created.
 * The type of the created bean owns an annotation based on {@link de.adito.ojcms.beans.annotations.ObserveCreation}.
 *
 * @param <ANNOTATION> the runtime type of the annotation (implicitly based on the annotation mentioned above)
 * @author Simon Danner, 26.12.2018
 * @see BeanCreationEvents
 */
public class BeanCreationEvent<ANNOTATION extends Annotation>
{
  private final IBean<?> createdBean;
  private final ANNOTATION annotation;

  /**
   * Creates a new creation event.
   *
   * @param pCreatedBean the created bean instance
   * @param pAnnotation  the creation observer annotation of the bean type, see {@link de.adito.ojcms.beans.annotations.ObserveCreation}
   */
  public BeanCreationEvent(IBean<?> pCreatedBean, ANNOTATION pAnnotation)
  {
    createdBean = pCreatedBean;
    annotation = pAnnotation;
  }

  /**
   * The newly created bean.
   *
   * @return a bean instance
   */
  public IBean<?> getCreatedBean()
  {
    return createdBean;
  }

  /**
   * The annotation instance that led to the observation.
   *
   * @return an annotation instance based on {@link de.adito.ojcms.beans.annotations.ObserveCreation}.
   */
  public ANNOTATION getCreationAnnotation()
  {
    return annotation;
  }
}
