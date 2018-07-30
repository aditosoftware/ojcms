package de.adito.beans.core.annotations;

import de.adito.beans.core.IBean;
import de.adito.beans.core.fields.IAdditionalFieldInfo;

import java.lang.annotation.*;
import java.util.function.Predicate;

/**
 * Marks a bean field as an optional field.
 * This annotation also provides a predicate, which determines when this field is active.
 * If this field isn't active according to the predicate, the bean behaves like this field isn't existing at all.
 * But it is still allowed to change the value of a non active bean field, because the condition may be based on the value.
 *
 * @author Simon Danner, 17.08.2017
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalField
{
  /**
   * Identifier for the predicate that will be stored at the field as additional information.
   */
  IAdditionalFieldInfo<IActiveCondition> ACTIVE_CONDITION = () -> IActiveCondition.class;

  /**
   * The condition that determines, when the bean-field should be active.
   * The type can be private. An instance will be created via reflection.
   * The type must define a default constructor.
   *
   * @return the type of the condition
   */
  Class<? extends IActiveCondition> value();

  /**
   * Describes a predicate that determines, when the bean field should be active.
   *
   * @param <BEAN> the type of the bean, which the field belongs to
   */
  interface IActiveCondition<BEAN extends IBean<BEAN>> extends Predicate<BEAN>
  {
  }
}
