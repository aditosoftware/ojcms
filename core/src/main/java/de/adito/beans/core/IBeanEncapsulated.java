package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Definiert den Daten-Kern für einen Bean.
 * Dieser wird als Map von IField nach Wert abgebildet.
 *
 * @param <BEAN> der generische Typ der Bean, welche zu diesem Datenkern gehört
 * @author s.danner, 20.01.2017
 */
interface IBeanEncapsulated<BEAN extends IBean<BEAN>> extends IEncapsulated<Map.Entry<IField<?>, Object>, BEAN, IBeanChangeListener<BEAN>>
{
  /**
   * Liefert den Wert zu einem Feld.
   *
   * @param pField das Bean-Feld, zu dem der Wer geliefert werden sollte
   * @param <TYPE> der Daten-Typ des Feldes
   * @return der Wert zum Feld
   */
  <TYPE> TYPE getValue(IField<TYPE> pField);

  /**
   * Setzt den Wert zu einem Bean-Feld neu.
   *
   * @param pField des betreffende Bean-Feld
   * @param pValue der neue Wert
   * @param <TYPE> der Daten-Typ des Feldes
   */
  <TYPE> void setValue(IField<TYPE> pField, TYPE pValue);

  /**
   * Entfernt ein Feld vom Daten-Kern
   *
   * @param pField das Feld, welches entfernt werden soll
   * @param pIndex der Index an dem das Feld hinzugefügt werden soll
   * @param <TYPE> der Daten-Typ des Feldes
   */
  <TYPE> void addField(IField<TYPE> pField, int pIndex);

  /**
   * Entfernt ein Feld vom Daten-Kern
   *
   * @param pField das Feld, welches entfernt werden soll
   * @param <TYPE> der Daten-Typ des Feldes
   */
  <TYPE> void removeField(IField<TYPE> pField);

  /**
   * Liefert den Index eines Feldes.
   *
   * @param pField das Bean-Feld
   * @param <TYPE> der Datentyp des Feldes
   * @return der Index des Feldes
   */
  <TYPE> int getFieldIndex(IField<TYPE> pField);

  /**
   * Liefert die Anzahl der Felder.
   */
  int getFieldCount();

  /**
   * Liefert die statistischen Daten dieses Kerns als Map
   *
   * @return eine Map mit einem IField als Key und den statistischen Daten zum Feld als Value
   */
  Map<IField<?>, IStatisticData> getStatisticData();

  /**
   * Liefert einen Stream der Felder dieses Kerns
   */
  Stream<IField<?>> streamFields();

  /**
   * Gibt an, ob in diesem Kern ein bestimmtes Bean-Feld enthalten ist.
   *
   * @param pField das gesuchte Bean-Feld
   * @param <TYPE> der Datentyp des Feldes
   * @return <tt>true</tt>, wenn das Feld vorhanden ist
   */
  default <TYPE> boolean containsField(IField<TYPE> pField)
  {
    return streamFields()
        .anyMatch(pExistingBean -> pExistingBean == pField); //Referenzen vergleichen wegen static Definition von Feldern
  }

  /**
   * Liefert die hierarchische Referenzen-Struktur dieses Kerns / dieser Bean.
   *
   * @return eine Schnittstelle zum Abfragen der Struktur
   */
  @Override
  default IHierarchicalBeanStructure getHierarchicalStructure()
  {
    return new HierarchicalBeanStructureImpl<>(this);
  }

  /**
   * Struktur mit mehr Funktionalität mit Bean-Referenzen (siehe Interface)
   *
   * @see IHierarchicalBeanStructure
   */
  class HierarchicalBeanStructureImpl<C, B extends IBean<B>, L extends IBeanChangeListener<B>> extends HierarchicalStructureImpl<C, B, L>
      implements IHierarchicalBeanStructure
  {
    public HierarchicalBeanStructureImpl(IEncapsulated<C, B, L> pEncapsulated)
    {
      super(pEncapsulated);
    }
  }
}
