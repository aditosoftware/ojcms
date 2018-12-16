package de.adito.ojcms.beans.annotations;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.fields.util.IAdditionalFieldInfo;

import java.lang.annotation.*;
import java.util.function.Predicate;

/**
 * Marks a bean field as an optional field.
 * This annotation also provides a predicate to determine when this field is active.
 * If this field isn't active according to the predicate, the bean behaves like this field isn't existing at all.
 * But it is still allowed to change the value of a non active bean field, because the condition may be based on the value.
 *
 * @author Simon Danner, 17.08.2017
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalField
{
  /**
   * Identifier for the predicate that will be stored at the field as an additional information.
   */
  IAdditionalFieldInfo<IActiveCondition> ACTIVE_CONDITION = () -> IActiveCondition.class;

  /**
   * The type of the condition determining when the bean field should be active.
   * It may be declared privately. An instance will be created via reflection.
   * It must define a default constructor.
   *
   * @return the type of the condition
   */
  Class<? extends IActiveCondition> value();

  /**
   * A predicate determining when the bean field should be active.
   *
   * @param <BEAN> the type of the bean, which the field belongs to
   */
  interface IActiveCondition<BEAN extends IBean<BEAN>> extends Predicate<BEAN>
  {
  }
}
