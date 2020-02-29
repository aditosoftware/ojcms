package de.adito.ojcms.cdi;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.*;

/**
 * CDI scope for unit test methods. The CDI scope lives as long as the execution of a test method.
 *
 * @author Simon Danner, 29.02.2020
 */
@Documented
@NormalScope
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface TestMethodScoped
{
}
