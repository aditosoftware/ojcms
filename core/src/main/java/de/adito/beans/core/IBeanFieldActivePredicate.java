package de.adito.beans.core;

import de.adito.beans.core.annotations.OptionalField;

/**
 * Bestimmt, ob ein optionales Bean-Feld gerade aktiv ist.
 * Muss die Bean bereitstellen.
 *
 * @author s.danner, 18.08.2017
 */
interface IBeanFieldActivePredicate<BEAN extends IBean<BEAN>>
{
  /**
   * Liefert die zugehörige Bean.
   */
  BEAN getBean();

  /**
   * Bestimmt, ob ein optionales Bean-Feld gerade aktiv ist.
   *
   * @param pField das betreffende Feld
   * @return <tt>true</tt>, wenn das Feld nicht optional ist oder es gerade aktiv ist
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
