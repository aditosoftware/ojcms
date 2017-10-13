package de.adito.beans.core.annotations;

import de.adito.beans.core.IAdditionalFieldInfo;
import de.adito.beans.core.IBean;

import java.lang.annotation.*;
import java.util.function.Predicate;

/**
 * Markiert ein Bean-Feld als optional.
 * Dieses existiert dann nur noch unter einer bestimmten Bedingung bzw. unter einem Zustand der Bean.
 *
 * @author s.danner, 17.08.2017
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalField
{
  IAdditionalFieldInfo<IActiveCondition> ACTIVE_CONDITION = () -> IActiveCondition.class;

  /**
   * Hier wird der Typ der Klasse angegeben, welche den Existenz-Zustand festlegt.
   *
   * @return der Typ einer Klasse, welche ein Predicate darstellt
   */
  Class<? extends IActiveCondition> value();

  /**
   * Beschreibt die Bedingung, wann ein Bean-Feld aktiv ist.
   */
  interface IActiveCondition<BEAN extends IBean<BEAN>> extends Predicate<BEAN>
  {
  }
}
