package de.adito.ojcms.transactions;

import de.adito.ojcms.cdi.*;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;

import java.lang.annotation.Annotation;

/**
 * Custom CDI context for transactions. The context applies to {@link TransactionalScoped} annotated types.
 *
 * @author Simon Danner, 27.12.2019
 */
@CustomCdiContext
public class TransactionContext extends AbstractCustomContext
{
  @Override
  public Class<? extends Annotation> getScope()
  {
    return TransactionalScoped.class;
  }
}
