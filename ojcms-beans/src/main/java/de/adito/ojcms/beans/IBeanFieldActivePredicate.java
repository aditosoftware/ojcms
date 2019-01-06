package de.adito.ojcms.beans;

import de.adito.ojcms.beans.literals.fields.IField;

import java.util.function.BiPredicate;

/**
 * Determines, if an optional bean field is active at a certain time.
 * The actual condition is deposited by the field itself.
 * However, this condition needs a bean to be applied to.
 * So an implementing class only has to provide the actual bean, where the active status of the field should be checked.
 * This leads to a very comfortable way to use this interface, especially if implemented as lambda expression.
 * For example, the bean interface may simply implement this interface as the following: "return () -> (BEAN) this;"
 *
 * @param <BEAN> the runtime type of the bean this field active condition is for
 * @author Simon Danner, 18.08.2017
 */
interface IBeanFieldActivePredicate<BEAN extends IBean<BEAN>>
{
  /**
   * The bean for which the active status of a certain field should be checked.
   *
   * @return the bean to check
   */
  BEAN getBean();

  /**
   * Determines, if an optional bean field is active at this moment.
   * If the field is not marked as optional, it will be treated as active.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the bean field
   * @return <tt>true</tt>, if the field is not optional or active, according the a given condition.
   */
  default <VALUE> boolean isOptionalActive(IField<VALUE> pField)
  {
    if (!pField.isOptional())
      return true;

    //noinspection unchecked
    final BiPredicate<BEAN, VALUE> condition = pField.getAdditionalInformationOrThrow(BeanFieldFactory.OPTIONAL_FIELD_INFO);
    assert condition != null;
    final BEAN bean = getBean();
    return condition.test(bean, bean.getValue(pField));
  }
}
