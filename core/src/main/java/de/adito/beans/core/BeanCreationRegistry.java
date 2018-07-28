package de.adito.beans.core;

import de.adito.beans.core.annotations.ObserveCreation;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * Central registration for bean creation events.
 * A bean annotated with {@link ObserveCreation} or another annotation annotated with {@link ObserveCreation}
 * will fire an event, when its constructor is called.
 * Here, it is possible to register listeners to react to those creation events.
 *
 * @author Simon Danner, 09.02.2018
 */
public final class BeanCreationRegistry
{
  private static final Map<Class<? extends IBean>, Set<Consumer<IBean<?>>>> TYPE_LISTENERS = new HashMap<>();
  private static final Map<Class<? extends Annotation>, Set<BiConsumer<IBean<?>, Annotation>>> ANNOTATION_LISTENERS = new HashMap<>();

  private BeanCreationRegistry()
  {
  }

  /**
   * Registers a bean creation listener for a certain bean type.
   *
   * @param pBeanType the bean's type to which the listener should react
   * @param pListener the creation listener
   * @param <BEAN>    the generic bean type
   */
  public static <BEAN extends IBean<BEAN>> void listenByBeanType(Class<BEAN> pBeanType, Consumer<BEAN> pListener)
  {
    _getObservableAnnotation(pBeanType); //check, if annotation is present
    synchronized (TYPE_LISTENERS)
    {
      //noinspection unchecked
      TYPE_LISTENERS.computeIfAbsent(pBeanType, pType -> new HashSet<>()).add((Consumer<IBean<?>>) pListener);
    }
  }

  /**
   * Registers a bean creation listener for beans annotated with a certain {@link ObserveCreation} annotation.
   *
   * @param pAnnotationType the annotation's type for which the listener should be registered
   * @param pListener       the creation listener
   * @param <ANNOTATION>    the generic annotation type
   */
  public static <ANNOTATION extends Annotation> void listenByAnnotation(Class<ANNOTATION> pAnnotationType, BiConsumer<IBean<?>, ANNOTATION> pListener)
  {
    if (!_isObservableAnnotation(pAnnotationType))
      throw new _NotObservableException(pAnnotationType.getName() + " is not a valid observable annotation.");
    synchronized (ANNOTATION_LISTENERS)
    {
      //noinspection unchecked
      ANNOTATION_LISTENERS.computeIfAbsent(pAnnotationType, pType -> new HashSet<>()).add((BiConsumer<IBean<?>, Annotation>) pListener);
    }
  }

  /**
   * Unregisters a creation listener for a certain bean type.
   *
   * @param pBeanType the bean type
   * @param pListener the listener to unregister
   * @param <BEAN>    the generic bean type
   */
  public static <BEAN extends IBean<BEAN>> void unlistenByBeanType(Class<BEAN> pBeanType, Consumer<BEAN> pListener)
  {
    if (!TYPE_LISTENERS.containsKey(pBeanType))
      return;

    synchronized (TYPE_LISTENERS)
    {
      //noinspection SuspiciousMethodCalls
      TYPE_LISTENERS.get(pBeanType).remove(pListener);
    }
  }

  /**
   * Unregisters a creation listener for beans annotated with a certain {@link ObserveCreation} annotation.
   *
   * @param pAnnotationType the annotation type for which the listener has been registered
   * @param pListener       the listener to unregister
   */
  public static <ANNOTATION extends Annotation> void unlistenByAnnotation(Class<ANNOTATION> pAnnotationType, BiConsumer<IBean<?>, ANNOTATION> pListener)
  {
    if (!ANNOTATION_LISTENERS.containsKey(pAnnotationType))
      return;

    synchronized (ANNOTATION_LISTENERS)
    {
      //noinspection SuspiciousMethodCalls
      ANNOTATION_LISTENERS.get(pAnnotationType).remove(pListener);
    }
  }

  /**
   * Fires a bean creation event with an annotation check.
   *
   * @param pCreatedBean the created bean
   */
  static void fireCreationIfAnnotationPresent(IBean<?> pCreatedBean)
  {
    if (_listenerPresent() && hasObservableAnnotation(pCreatedBean.getClass()))
      fireCreation(pCreatedBean);
  }

  /**
   * Fires a bean creation event.
   *
   * @param pCreatedBean the created bean
   */
  static void fireCreation(IBean<?> pCreatedBean)
  {
    if (!_listenerPresent())
      return;

    Class<? extends IBean> beanType = pCreatedBean.getClass();
    Class<? extends Annotation> annotationType = _getObservableAnnotation(beanType);
    //Fire to type listeners
    synchronized (TYPE_LISTENERS)
    {
      if (TYPE_LISTENERS.containsKey(beanType))
        TYPE_LISTENERS.get(beanType).forEach(pListener -> pListener.accept(pCreatedBean));
    }
    //Fire to annotation listeners
    synchronized (ANNOTATION_LISTENERS)
    {
      if (ANNOTATION_LISTENERS.containsKey(annotationType))
        ANNOTATION_LISTENERS.get(annotationType).forEach(pListener -> pListener.accept(pCreatedBean, beanType.getAnnotation(annotationType)));
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
   * Determines, if any listener is registered.
   *
   * @return <tt>true</tt>, if there's at least one listener registered
   */
  private static boolean _listenerPresent()
  {
    return !TYPE_LISTENERS.isEmpty() || !ANNOTATION_LISTENERS.isEmpty();
  }

  /**
   * The observable annotation from the created bean type.
   * A bean has to be provided with that annotation to listen to creation events.
   *
   * @param pBeanType the bean type
   * @return the observable annotation of the bean type
   * @throws _NotObservableException if the annotation is not present
   */
  private static Class<? extends Annotation> _getObservableAnnotation(Class<? extends IBean> pBeanType)
  {
    //noinspection unchecked
    return _searchObservableAnnotation(pBeanType)
        .orElseThrow(() -> new _NotObservableException(pBeanType));
  }

  /**
   * Searches a observable annotation from a bean type.
   * The annotation is either {@link ObserveCreation} itself or annotated with {@link ObserveCreation}.
   *
   * @param pBeanType the bean type
   * @return the optional observable annotation
   */
  private static Optional<Class<? extends Annotation>> _searchObservableAnnotation(Class<? extends IBean> pBeanType)
  {
    return Stream.of(pBeanType.getDeclaredAnnotations())
        .map(Annotation::annotationType)
        .filter(BeanCreationRegistry::_isObservableAnnotation)
        .findAny()
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

  /**
   * Exception for non observable beans.
   */
  private static class _NotObservableException extends RuntimeException
  {
    public _NotObservableException(Class<? extends IBean> pBeanType)
    {
      super(pBeanType.getName() + " is not a observable bean type.");
    }

    public _NotObservableException(String pMessage)
    {
      super(pMessage);
    }
  }
}
