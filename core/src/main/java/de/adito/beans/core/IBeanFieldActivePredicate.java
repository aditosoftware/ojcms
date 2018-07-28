package de.adito.beans.core;

import de.adito.beans.core.annotations.OptionalField;

/**
 * Determines, if an optional bean field is active at a certain time.
 * The actual condition is deposited by the field itself.
 * However, this condition needs a bean to be applied to.
 * So an implementing class only has to provide the actual bean, where the active status of the field should be checked.
 * This leads to a very comfortable way to use this interface, especially if implemented as lambda expression.
 * For example, the bean interface may simply implement this interface as the following: "return () -> (BEAN) this;"
 *
 * @author Simon Danner, 18.08.2017
 */
interface IBeanFieldActivePredicate<BEAN extends IBean<BEAN>>
{
  /**
   * The bean for which the active status of a certain field should be checked.
   */
  BEAN getBean();

  /**
   * Determines, if an optional bean field is active at this moment.
   * If the field is not marked as optional, it will be treated as active.
   *
   * @param pField the bean field
   * @return <tt>true</tt>, if the field is not optional or active, according the a given condition.
   */
  default boolean isOptionalActive(IField<?> pField)
  {
    if (!pField.isOptional())
      return true;

    //noinspection unchecked
    OptionalField.IActiveCondition<BEAN> condition = pField.getAdditionalInformation(OptionalField.ACTIVE_CONDITION);
    assert condition != null;
    return condition.test(getBean());
  }
}
