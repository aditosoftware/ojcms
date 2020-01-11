package de.adito.ojcms.cdi;

import de.adito.ojcms.cdi.context.*;

import javax.enterprise.context.*;
import java.lang.annotation.Annotation;

/**
 * Injectable interface to control CDI elements or to create CDI managed instances programmatically.
 *
 * @author Simon Danner, 27.12.2019
 */
public interface ICdiControl
{
  /**
   * Creates an instances of a specific type. The type must be managed by the CDI container.
   *
   * @param pType the type to create a CDI managed instance of
   * @param pQualifiers qualifier annotations for the instance to create
   * @return the created instance
   */
  <T> T createInjected(Class<T> pType, Annotation... pQualifiers);

  /**
   * Injects an existing instance. An excessive use of this method may hint some bad design. Think about it.
   *
   * @param pInstance the instance to inject
   * @return the injected instance
   */
  <T> T injectInstance(T pInstance);

  /**
   * Starts a custom context. Custom contexts must extend {@link AbstractCustomContext} and be annotated by {@link CustomCdiContext}.
   *
   * @param pScopeAnnotationType the scope's annotation type to identify the context's type (see {@link NormalScope}
   * @return a control element of the started context (shutdown)
   */
  IActiveContext startContext(Class<? extends Annotation> pScopeAnnotationType);

  /**
   * Determines if a custom context of a specific type is active currently.
   *
   * @param pScopeAnnotationType the scope's annotation type to identify the context's type (see {@link NormalScope}
   * @return <tt>true</tt> if the context is active
   */
  boolean isContextActive(Class<? extends Annotation> pScopeAnnotationType);

  /**
   * Terminates the CDI container.
   */
  void shutdown();
}
