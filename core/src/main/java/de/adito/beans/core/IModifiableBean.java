package de.adito.beans.core;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;

/**
 * Definiert eine modifizierbare Bean, welche anhand von Feldern erweitert und reduziert werden kann.
 *
 * @param <BEAN> der generische Typ der speziellen Bean, welche mit diesem Interface ausgestattet ist (kann nur eine Hülle sein, daher IBean als Basis)
 * @author s.danner, 01.02.2017
 */
public interface IModifiableBean<BEAN extends IBean<BEAN>> extends IBean<BEAN>
{
  /**
   * Erweitert die Bean um ein Feld.
   *
   * @param pFieldType   der Typ des neuen Feldes
   * @param pName        der Name des neuen Feldes
   * @param pAnnotations die Annotationen des neuen Feldes
   * @param <TYPE>       der Daten-Typ des neuen Feldes
   * @return das neu erstelle Feld
   */
  default <TYPE, FIELD extends IField<TYPE>> FIELD addField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    if (encapsulated.streamFields().anyMatch(pField -> pField.getName().equals(pName)))
      throw new RuntimeException("field: " + pName);
    FIELD newField = BeanFieldFactory.createField(pFieldType, pName, pAnnotations);
    addField(newField);
    return newField;
  }

  /**
   * Erweitert die Bean um ein bereits existentes Feld.
   *
   * @param pField das Feld, welches hinzugefügt werden soll
   * @param <TYPE> der Datentyp des Feldes
   */
  default <TYPE> void addField(IField<TYPE> pField)
  {
    addField(pField, getFieldCount());
  }

  /**
   * Erweitert die Bean um ein bereits existentes Feld.
   *
   * @param pField das Feld, welches hinzugefügt werden soll
   * @param pIndex der Index an dem das Feld hinzugefügt werden soll
   * @param <TYPE> der Datentyp des Feldes
   */
  default <TYPE> void addField(IField<TYPE> pField, int pIndex)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    encapsulated.addField(pField, pIndex);
    if (getFieldActiveSupplier().isOptionalActive(pField))
      //noinspection unchecked
      encapsulated.fire(pListener -> pListener.fieldAdded((BEAN) this, pField));
  }

  /**
   * Reduziert die Bean um ein Feld.
   *
   * @param pField das zu entfernende Feld
   * @param <TYPE> der Datentyp des Feldes
   */
  default <TYPE> void removeField(IField<TYPE> pField)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    TYPE oldValue = encapsulated.getValue(pField);
    encapsulated.removeField(pField);
    //noinspection unchecked
    encapsulated.fire(pListener -> pListener.fieldRemoved((BEAN) this, pField, oldValue));
  }

  /**
   * Entfernt ein Feld, wenn es dem übergebenem Prädikat entspricht
   *
   * @param pFieldPredicate das Prädikat, welches bestimmt, welche Felder entfernt werden sollen
   * @param <TYPE>          der innere Datentyp eines Feldes
   */
  default <TYPE> void removeFieldIf(Predicate<IField<TYPE>> pFieldPredicate)
  {
    Iterator<Map.Entry<IField<?>, Object>> it = getEncapsulated().iterator();
    while (it.hasNext())
      //noinspection unchecked
      if (pFieldPredicate.test((IField<TYPE>) it.next().getKey()))
        it.remove();
  }
}
