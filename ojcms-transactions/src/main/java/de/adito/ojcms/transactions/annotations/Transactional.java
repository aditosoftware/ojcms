package de.adito.ojcms.transactions.annotations;

import de.adito.ojcms.transactions.api.ConcurrentTransactionException;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Interceptor binding to define transaction scopes for methods or indirectly for all public methods of class.
 * If some annotated method is invoked a new transaction will be started internally.
 * The instance the method is called on must be managed by the CDI container.
 *
 * @author Simon Danner, 25.12.2019
 * @see TransactionalScoped
 */
@Documented
@InterceptorBinding
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Transactional
{
  /**
   * Defines the mode of the transaction.
   *
   * @return the defined transaction mode or a default value
   */
  @Nonbinding
  ETransactionMode mode() default ETransactionMode.REQUIRES_NEW;

  /**
   * The amount of retries in case of a {@link ConcurrentTransactionException} during a transaction.
   *
   * @return the defined amount of retries or a default value
   */
  @Nonbinding
  int tries() default 5;

  /**
   * Defines a timeout for the transaction.
   *
   * @return the defined timeout in milliseconds or a default value
   */
  @Nonbinding
  long timeout() default 2 * 60 * 1000;
}
