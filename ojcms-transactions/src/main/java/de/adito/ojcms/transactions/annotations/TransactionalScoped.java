package de.adito.ojcms.transactions.annotations;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.*;

/**
 * Used to define the scope of some CDI managed type to be alive as long as the current transaction.
 * This annotation may also been used on producer methods or fields.
 *
 * @author Simon Danner, 25.12.2019
 * @see Transactional
 */
@Documented
@NormalScope
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface TransactionalScoped
{
}
