package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.ObserveCreation;
import de.adito.ojcms.beans.exceptions.bean.BeanCreationNotObservableException;
import de.adito.ojcms.beans.reactive.events.BeanCreationEvent;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Central registration for bean creation events.
 * A bean annotated with {@link ObserveCreation} or another annotation annotated with {@link ObserveCreation}
 * will fire an event, when its constructor is called.
 * This will only work for beans based on {@link OJBean}.
 *
 * @author Simon Danner, 09.02.2018
 */
public final class BeanCreationEvents
{
  private static final Map<Class<? extends IBean>, PublishSubject<IBean>> PUBLISHERS_BY_TYPE = new ConcurrentHashMap<>();
  private static final Map<Class<? extends Annotation>, PublishSubject<? extends BeanCreationEvent<?>>> PUBLISHERS_BY_ANNOTATION =
      new ConcurrentHashMap<>();

  private BeanCreationEvents()
  {
  }

  /**
   * An observable for newly created beans for specific bean types.
   *
   * @param pBeanType the bean's type to observe creations of
   * @param <BEAN>    the generic runtime type of the bean
   */
  public static <BEAN extends IBean> Observable<BEAN> observeCreationByBeanType(Class<BEAN> pBeanType)
  {
    _getObservableAnnotation(pBeanType); //check, if annotation is present
    //noinspection unchecked
    return (Observable<BEAN>) PUBLISHERS_BY_TYPE.computeIfAbsent(pBeanType, pType -> PublishSubject.create()) //
        .observeOn(Schedulers.newThread());
  }

  /**
   * An observable for newly created beans for bean types which are annotated by {@link ObserveCreation}.
   *
   * @param pAnnotationType the annotation's type for which creation events should be observed
   * @param <ANNOTATION>    the generic annotation type
   */
  public static <ANNOTATION extends Annotation> Observable<BeanCreationEvent<ANNOTATION>> observeCreationByAnnotationType(
      Class<ANNOTATION> pAnnotationType)
  {
    if (!_isObservableAnnotation(pAnnotationType))
      throw new BeanCreationNotObservableException(pAnnotationType.getName() + " is not a valid creation observer annotation.");
    //noinspection unchecked
    return (Observable<BeanCreationEvent<ANNOTATION>>) PUBLISHERS_BY_ANNOTATION.computeIfAbsent(pAnnotationType,
        pType -> PublishSubject.create()).observeOn(Schedulers.newThread());
  }

  /**
   * Fires a bean creation event with an annotation check.
   *
   * @param pCreatedBean the newly created bean
   */
  static void fireCreationIfAnnotationPresent(IBean pCreatedBean)
  {
    if (_observersPresent() && hasObservableAnnotation(pCreatedBean.getClass()))
      fireCreation(pCreatedBean);
  }

  /**
   * Fires a bean creation event.
   *
   * @param pCreatedBean the created bean
   */
  static void fireCreation(IBean pCreatedBean)
  {
    if (!_observersPresent())
      return;

    final Class<? extends IBean> beanType = pCreatedBean.getClass();
    final Class<? extends Annotation> annotationType = _getObservableAnnotation(beanType);
    //Fire to type observers
    if (PUBLISHERS_BY_TYPE.containsKey(beanType))
      PUBLISHERS_BY_TYPE.get(beanType).onNext(pCreatedBean);
    //Fire to annotation observers
    if (PUBLISHERS_BY_ANNOTATION.containsKey(annotationType))
    {
      //noinspection unchecked
      final PublishSubject<BeanCreationEvent<?>> publisher = (PublishSubject<BeanCreationEvent<?>>) PUBLISHERS_BY_ANNOTATION.get(
          annotationType);

      publisher.onNext(new BeanCreationEvent<>(pCreatedBean, beanType.getAnnotation(annotationType)));
    }
  }

  /**
   * Determines, if a bean type has a {@link ObserveCreation} or has another annotation annotated with {@link ObserveCreation}.
   *
   * @param pBeanType the bean type
   * @return <tt>true</tt>, if the annotation is present
   */
  static boolean hasObservableAnnotation(Class<? extends IBean> pBeanType)
  {
    return _searchObservableAnnotation(pBeanType).isPresent();
  }

  /**
   * Determines if observers for creation events are present at this moment.
   *
   * @return <tt>true</tt>, if there's at least one observer active
   */
  private static boolean _observersPresent()
  {
    return Stream.concat(PUBLISHERS_BY_TYPE.values().stream(), PUBLISHERS_BY_ANNOTATION.values().stream()) //
        .anyMatch(PublishSubject::hasObservers);
  }

  /**
   * The observable annotation from the created bean type.
   * A bean has to be provided with that annotation to listen to creation events.
   *
   * @param pBeanType the bean type
   * @return the observable annotation of the bean type
   * @throws BeanCreationNotObservableException if the annotation is not present
   */
  private static Class<? extends Annotation> _getObservableAnnotation(Class<? extends IBean> pBeanType)
  {
    return _searchObservableAnnotation(pBeanType).orElseThrow(() -> new BeanCreationNotObservableException(pBeanType));
  }

  /**
   * Tries to find an observable annotation from a bean type.
   * The annotation is either {@link ObserveCreation} itself or annotated with {@link ObserveCreation}.
   *
   * @param pBeanType the bean type
   * @return the optional observable annotation
   */
  private static Optional<Class<? extends Annotation>> _searchObservableAnnotation(Class<? extends IBean> pBeanType)
  {
    return Stream.of(pBeanType.getDeclaredAnnotations()) //
        .map(Annotation::annotationType) //
        .filter(BeanCreationEvents::_isObservableAnnotation) //
        .findAny() //
        .map(pAnnotationType -> (Class<? extends Annotation>) pAnnotationType);
  }

  /**
   * Determines, if any annotation type is an observable annotation.
   * Either it is {@link ObserveCreation} itself or annotated with {@link ObserveCreation}
   *
   * @param pAnnotationType the annotation type
   * @return <tt>true</tt>, if it is an observable annotation
   */
  private static boolean _isObservableAnnotation(Class<? extends Annotation> pAnnotationType)
  {
    return pAnnotationType == ObserveCreation.class || pAnnotationType.isAnnotationPresent(ObserveCreation.class);
  }
}
