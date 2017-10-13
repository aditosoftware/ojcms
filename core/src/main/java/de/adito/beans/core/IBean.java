package de.adito.beans.core;

import de.adito.beans.core.annotations.Identifier;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.BeanFlattenUtil;
import de.adito.beans.core.util.BeanUtil;
import de.adito.beans.core.util.IBeanFieldPredicate;
import de.adito.beans.core.util.exceptions.FieldDoesNotExistException;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.*;

/**
 * Beschreibt die Hülle einer Bean.
 * Eine Bean besteht aus einem abgekapselten Daten-Kern und der Hülle, welche über default-Methoden sämtliche Funktionalität bereitstellt.
 * Hier wird bewusst ein Interface als Hülle verwendet, um beliebige Objekte als Bean behandeln zu können.
 *
 * Der Daten-Kern wird über die einzig nicht default-Methode geliefert.
 * Diese Methode kann auch als 'virtual-field' bezeichnet werden, da sich alle default-Methoden so Zugriff auf den Kern verschaffen.
 * Da der Daten-Kern gekapselt werden soll und Interfaces nur public-Methoden besitzen können, ist der Datenkern package-protected und
 * somit nur von 'innen' zugänglich.
 *
 * @param <BEAN> der generische Typ der speziellen Bean, welche mit diesem Interface ausgestattet ist
 * @author s.danner, 23.08.2016
 */
public interface IBean<BEAN extends IBean<BEAN>> extends IEncapsulatedHolder<IBeanEncapsulated<BEAN>>
{
  /**
   * Liefert den Wert zu einem Bean-Feld.
   * Ist nur möglich, wenn das Feld nicht private ist.
   *
   * @param pField das Bean-Feld
   * @param <TYPE> der Daten-Typ des Feldes
   * @return der Wert des Feldes
   */
  default <TYPE> TYPE getValue(IField<TYPE> pField)
  {
    if (!hasField(pField))
      throw new FieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new UnsupportedOperationException();
    assert getEncapsulated() != null;
    return getEncapsulated().getValue(pField);
  }

  /**
   * Liefert den Wert zu einem Bean-Feld, wenn dieser nicht null ist.
   * Sonst wird der Default-Wert des Feldes zurückgegeben.
   * Ist nur möglich, wenn das Feld nicht private ist.
   *
   * @param pField das Bean-Feld
   * @param <TYPE> er Daten-Typ des Feldes
   * @return der Wert des Feldes oder der Default-Wert, wenn null
   */
  default <TYPE> TYPE getValueOrDefault(IField<TYPE> pField)
  {
    return Optional.ofNullable(getValue(pField)).orElse(pField.getDefaultValue());
  }

  /**
   * Liefert den Wert zu einem Bean-Feld.
   * Ist nur möglich, wenn das Feld nicht private ist.
   * Hier kann zusätzlich ein Typ angegeben werden, zu welchem der Wert konvertiert werden soll.
   * Dies ist nur möglich, wenn das Feld einen geeigneten Konverter zur Verfügung stellt.
   *
   * @param pField       das Bean-Feld
   * @param pConvertType der Typ, zu welchem der Wert konvertiert werden soll
   * @param <TYPE>       der Daten-Typ des Feldes
   * @return der konvertierte Wert des Feldes
   */
  default <TYPE, SOURCE> SOURCE getValueConverted(IField<TYPE> pField, Class<SOURCE> pConvertType)
  {
    TYPE actualValue = getValue(pField);
    if (actualValue == null || pConvertType.isAssignableFrom(actualValue.getClass()))
      //noinspection unchecked
      return (SOURCE) actualValue;
    return pField.getFromConverter(pConvertType)
        .orElseThrow(() -> new RuntimeException("type: " + pConvertType.getSimpleName()))
        .apply(actualValue);
  }

  /**
   * Setzt den Daten-Wert eines Bean-Feldes.
   * Ist nur möglich, wenn das Feld nicht private ist.
   * Informiert bei Wert-Veränderung alle registrierten Listener in einem neuen Thread.
   *
   * @param pField das Bean-Feld, für welches der Wert gesetzt werden soll
   * @param pValue der neue Wert
   * @param <TYPE> der Daten-Typ des Feldes
   */
  default <TYPE> void setValue(IField<TYPE> pField, TYPE pValue)
  {
    if (!hasField(pField))
      throw new FieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new UnsupportedOperationException();
    //noinspection unchecked
    BeanListenerUtil.setValueAndFire((BEAN) this, pField, pValue);
  }

  /**
   * Setzt den Daten-Wert eines Bean-Feldes.
   * Ist nur möglich, wenn das Feld nicht private ist.
   * Informiert bei Wert-Veränderung alle registrierten Listener in einem neuen Thread.
   * Hier wird der Wert (wenn nötig) vorher konvertiert, falls ein geeigneter Konverter vom Feld bereitgestellt wird.
   *
   * @param pField          das Bean-Feld, für welches der Wert gesetzt werden soll
   * @param pValueToConvert der Wert, welcher noch konvertiert werden muss
   * @param <TYPE>          der Daten-Typ des Feldes
   * @param <SOURCE>        der Typ des Wertes vor der Konvertierung
   */
  @SuppressWarnings("unchecked")
  default <TYPE, SOURCE> void setValueConverted(IField<TYPE> pField, SOURCE pValueToConvert)
  {
    TYPE convertedValue = null;
    if (pValueToConvert != null)
    {
      Class<SOURCE> sourceType = (Class<SOURCE>) pValueToConvert.getClass();
      convertedValue = pField.getType().isAssignableFrom(sourceType) ? (TYPE) pValueToConvert :
          pField.getToConverter(sourceType)
              .orElseThrow(() -> new RuntimeException("type: " + sourceType.getSimpleName()))
              .apply(pValueToConvert);
    }
    setValue(pField, convertedValue);
  }

  /**
   * Setzt alle Werte aller Felder dieses Beans auf null.
   */
  default void clear()
  {
    streamFields()
        .filter(pField -> !pField.isPrivate())
        .forEach(pField -> setValue(pField, null));
  }

  /**
   * Liefert ein Supplier, welcher ermittelt, ob ein optional Feld der Bean gerade aktiv oder inaktiv ist.
   */
  default IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    //noinspection unchecked
    return () -> (BEAN) this;
  }

  /**
   * Gibt an, ob diese Bean ein bestimmtes Feld besitzt
   *
   * @param pField das Feld, welches überprüft werden soll
   * @return <tt>true</tt>, wenn die Bean dieses Feld besitzt
   */
  default boolean hasField(IField pField)
  {
    //Referenzen vergleichen, da Felder static definiert
    return streamFields().anyMatch(pExistingField -> pField == pExistingField);
  }

  /**
   * Liefert die Anzahl der Felder der Bean.
   */
  default int getFieldCount()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getFieldCount();
  }

  /**
   * Liefert den Index eines Feldes der Bean.
   *
   * @param pField das Bean-Feld
   * @param <TYPE> der Datentyp des Feldes
   * @return der Index des Feldes
   */
  default <TYPE> int getFieldIndex(IField<TYPE> pField)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getFieldIndex(pField);
  }

  /**
   * Liefert die hierarchische Einordnung dieser Bean.
   * Dabei werden ausgehend von dieser Bean die Parent-Knoten bereitgestellt,
   * welche über ein Bean- oder Bean-Container-Feld auf diese Bean referenzieren.
   *
   * @return die Schnittstelle, um die Informationen zur hierarchischen Struktur zu erlangen
   */
  default IHierarchicalBeanStructure getHierarchicalStructure()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getHierarchicalStructure();
  }

  /**
   * Fügt einen Bean-Change-Listener hinzu.
   *
   * @param pListener der Listener, welcher über Wert-Änderungen des Beans informiert
   */
  default void listenWeak(IBeanChangeListener<BEAN> pListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addListener(pListener);
  }

  /**
   * Entfernt einen Bean-Change-Listener.
   *
   * @param pListener der Listener, welcher entfernt werden soll
   */
  default void unlisten(IBeanChangeListener<BEAN> pListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeListener(pListener);
  }

  /**
   * Liefert die statistischen Daten für ein Bean-Feld, wenn vorhanden, sonst null.
   *
   * @param pField des bestimmte Bean-Feld
   * @param <TYPE> der Daten-Typ des Feldes
   * @return die Statistiken des Feldes oder null, wenn nicht vorhanden
   */
  @Nullable
  default <TYPE> IStatisticData<TYPE> getStatisticData(IField<TYPE> pField)
  {
    if (!hasField(pField))
      return null;
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().getStatisticData().get(pField);
  }

  /**
   * Liefert alle Felder des Beans, welche ein Identifikator sind.
   */
  default Collection<IField<?>> getIdentifiers()
  {
    return streamFields()
        .filter(pField -> pField.hasAnnotation(Identifier.class))
        .collect(Collectors.toList());
  }

  /**
   * Sucht ein Feld dieser Bean über den Namen.
   *
   * @param pName der gesuchte Name des Feldes
   * @return ein Optional, welches das Resultat der Suche verkörpert
   */
  default Optional<IField<?>> findFieldByName(String pName)
  {
    return streamFields()
        .filter(pField -> pField.getName().equals(pName))
        .findAny();
  }

  /**
   * Erzeugt eine Kopie der Bean mit ausgeschlossenen Feldern.
   * Hier kann zusätzlich angegeben werden, ob die kopierte Bean automatisch geupdatet werden soll, wenn sich das Original verändert.
   *
   * @param pFieldPredicate das Predicate, welches bestimmt, welche Felder ausgeschlossen werden sollen
   * @param pUpdateChanges  <tt>true</tt>, wenn automatisch ein Listener für Änderungen an der Original-Bean eingehängt werden soll
   * @return die reduzierte Bean-Kopie
   */
  default IBean reducedCopy(IBeanFieldPredicate pFieldPredicate, boolean pUpdateChanges)
  {
    assert getEncapsulated() != null;
    IBean<?> reducedCopy = new BeanCopy(BeanUtil.asMap(this, pFieldPredicate), getFieldActiveSupplier());
    if (pUpdateChanges)
      reducedCopy = BeanListenerUtil.makeChangeAware(this, reducedCopy, false, pFieldPredicate);
    return reducedCopy;
  }

  /**
   * Erzeugt eine flache Kopie der Bean. (nur auf einer Ebene - nicht tief!!)
   * Dabei werden alle Felder, welche eine Bean beinhalten 'geflattet'.
   * Hier wird zusätzlich die Kopie informiert, wenn sich am Original ein Feld verändert.
   * Dieser Mechanismus ist allerdings nur möglich, wenn es sich um keine tiefe Kopie handelt,
   * da sonst nicht mehr nachvollziehbar ist, welches Feld zu welcher Parent-Bean im Original gehört.
   *
   * @return eine flache Kopie der Bean (eine Ebene)
   */
  default IBean flatCopyWithUpdates()
  {
    IBean flatCopy = flatCopy(false);
    return BeanListenerUtil.makeChangeAware(this, flatCopy, true, null);
  }

  /**
   * Erzeugt eine flache Kopie der Bean.
   * Dabei werden alle Felder, welche eine Bean beinhalten 'geflattet'.
   *
   * @param pDeep <tt>true</tt>, wenn dabei bis in die Tiefe 'geflattet' werden soll, sonst nur eine Ebene
   * @return eine flache Kopie der Bean
   */
  default IBean flatCopy(boolean pDeep)
  {
    assert getEncapsulated() != null;
    return new BeanCopy(BeanFlattenUtil.createFlatCopy(this, pDeep), getFieldActiveSupplier());
  }

  /**
   * Liefert alle Felder der Bean als Stream.
   */
  default Stream<IField<?>> streamFields()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().streamFields()
        .filter(pField -> getFieldActiveSupplier().isOptionalActive(pField));
  }

  /**
   * Liefert die Bean als Stream. (IField -> Value)
   */
  default Stream<Map.Entry<IField<?>, Object>> stream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().stream()
        .filter(pEntry -> getFieldActiveSupplier().isOptionalActive(pEntry.getKey()));
  }
}
