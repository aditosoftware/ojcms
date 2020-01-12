package de.adito.ojcms.sql.datasource.connection;

import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * CDI qualifier for {@link OJSQLBuilder} that can be used outside of transactions to perform database initialization.
 *
 * @author Simon Danner, 02.01.2020
 */
@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface GlobalBuilder
{
}
