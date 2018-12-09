package de.adito.ojcms.utils.readonly;

import java.lang.reflect.*;
import java.util.*;

/**
 * A read only invocation handler wrapping around a source instance and block all methods annotated with {@link WriteOperation}.
 * Also provides read only support for collection and map return types.
 *
 * @param <SOURCE> the type of the source instance to make read only
 * @author Simon Danner, 08.12.2018
 */
public class ReadOnlyInvocationHandler<SOURCE> implements InvocationHandler
{
  private final SOURCE source;

  /**
   * Creates a proxy instance of an interface type that blocks all write operations of an actual instance implementing the interface.
   *
   * @param pInterfaceType          the type of the interface to create a read only proxy for
   * @param pInstanceToMakeReadOnly the instance to make read only
   * @param <INTERFACE>             the generic type of the interface
   * @return the proxy instance of the interface blocking all write operations
   */
  public static <INTERFACE> INTERFACE createReadOnlyInstance(Class<INTERFACE> pInterfaceType, INTERFACE pInstanceToMakeReadOnly)
  {
    //noinspection unchecked
    return (INTERFACE) Proxy.newProxyInstance(pInterfaceType.getClassLoader(), new Class[]{pInterfaceType},
                                              new ReadOnlyInvocationHandler<>(pInstanceToMakeReadOnly));
  }

  /**
   * Creates the read only invocation handler.
   *
   * @param pSource the source to make read only
   */
  private ReadOnlyInvocationHandler(SOURCE pSource)
  {
    source = pSource;
  }

  @Override
  public Object invoke(Object pProxy, Method pMethod, Object[] pArgs) throws Throwable
  {
    if (pMethod.isAnnotationPresent(WriteOperation.class))
      throw new UnsupportedOperationException("This element of type " + source.getClass().getName() + " is read-only! " +
                                                  "The content can not be modified!");
    final Object result = pMethod.invoke(source, pArgs);
    if (result instanceof List)
      return Collections.unmodifiableList((List<?>) result);
    if (result instanceof Set)
      return Collections.unmodifiableSet((Set<?>) result);
    if (result instanceof Map)
      return Collections.unmodifiableMap((Map<?, ?>) result);
    return result;
  }
}
