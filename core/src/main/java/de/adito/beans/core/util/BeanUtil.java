package de.adito.beans.core.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * Allgemeine Utility-Klasse für das Bean-Modell
 *
 * @author s.danner, 29.06.2017
 */
public final class BeanUtil
{
  private BeanUtil()
  {
  }

  /**
   * Liefert die Bean als Map.
   * Zusätzlich kann ein Field-Predicate bestimmt werden, welches manche Felder ausschließt.
   *
   * @param pFieldPredicate das Field-Predicate, welches festlegt, welche Felder ausgeschlossen werden sollen
   * @return eine Map mit Bean-Feldern als Key und dem Wert als Value
   */
  public static Map<IField<?>, Object> asMap(IBean<?> pBean, IBeanFieldPredicate pFieldPredicate)
  {
    return pBean.stream()
        .filter(pEntry -> pFieldPredicate.test(pEntry.getKey(), pEntry.getValue()))
        //Hier eigener Collector, da der Standard-Map-Collector keine null values erlaubt
        .collect(LinkedHashMap::new, (pMap, pEntry) -> pMap.put(pEntry.getKey(), pEntry.getValue()), LinkedHashMap::putAll);
  }

  /**
   * Liefert ein Bean-Feld anhand des Feld-Namen innerhalb einer Bean.
   * Diese Methode davon aus, dass das Feld bei der Bean existiert, sonst Fehler!
   *
   * @param pBean      die Bean, bei welcher das Feld existiert
   * @param pFieldName der gesuchte Feld-Name
   * @return das Feld zu dem gesuchten Namen
   */
  public static IField<?> findFieldByName(IBean<?> pBean, String pFieldName)
  {
    return findFieldByName(pBean.streamFields(), pFieldName);
  }

  /**
   * Liefert ein Bean-Feld anhand des Feld-Namen.
   * Diese Methode davon aus, dass sich das Feld in dem Stream befindet, sonst Fehler!
   *
   * @param pFieldStream ein Stream von Feldern, in welchem gesucht werden soll
   * @param pFieldName   der gesuchte Feld-Name
   * @return das Feld zu dem gesuchten Namen
   */
  public static IField<?> findFieldByName(Stream<IField<?>> pFieldStream, String pFieldName)
  {
    return pFieldStream
        .filter(pField -> pField.getName().equals(pFieldName))
        .findAny()
        .orElseThrow(() -> new RuntimeException("name: " + pFieldName));
  }

  /**
   * Vergleicht die Werte zweier Beans anhand von bestimmten Feldern.
   * Hier wird davon ausgegangen, dass beide Beans alle zu vergleichenden Felder besitzen!
   *
   * @param pBean1         erste Bean für den Vergleich
   * @param pBean2         zweite Bean für den Vergleich
   * @param pFieldsToCheck eine Menge von Feldern, welche verglichen werden sollen
   * @return ein Optional, welches ein unterschiedliches Feld beinhaltet oder leer ist, wenn alle Werte gleich sind
   */
  public static Optional<IField> compareBeanValues(IBean pBean1, IBean pBean2, Collection<IField<?>> pFieldsToCheck)
  {
    return pFieldsToCheck.stream()
        .map(pField -> (IField) pField)
        .filter(pField -> !Objects.equals(pBean1.getValue(pField), pBean2.getValue(pField)))
        .findAny();
  }

  /**
   * Findet die äquivalente Bean aus einer Quell-Menge, welche zum Vergleich dient.
   * Zum Auffinden dieser Bean dienen die Felder die als @Identifier annotiert sind.
   * Wenn die Bean gefunden wurde, wird sie aus der Menge entfernt.
   *
   * @param pBean         die Bean, zu welcher das Äquivalent gefunden werden soll
   * @param pOldToCompare die Beans mit denen verglichen wird
   * @return die zugehörige äquivalente Bean (oder null, wenn nicht vorhanden)
   * @see de.adito.beans.core.annotations.Identifier
   */
  @Nullable
  public static <BEAN extends IBean<BEAN>> BEAN findRelatedBeanAndRemove(BEAN pBean, Collection<BEAN> pOldToCompare)
  {
    Iterator<BEAN> it = pOldToCompare.iterator();
    Collection<IField<?>> identifiers = pBean.getIdentifiers();
    while (it.hasNext())
    {
      BEAN oldBean = it.next();
      if (pBean.getClass() == oldBean.getClass() && //Typen müssen gleich sein
          ((identifiers.isEmpty() && Objects.equals(oldBean, pBean)) || //Wenn keine Identifier vorhanden -> equals()
              (identifiers.equals(oldBean.getIdentifiers()) && !BeanUtil.compareBeanValues(oldBean, pBean, identifiers).isPresent())))
      {
        it.remove();
        return oldBean;
      }
    }

    return null;
  }

  /**
   * Liefert eine tiefe Bean innerhalb einer Parent-Bean.
   * Die tiefe Bean wird anhand einer Folge von Bean-Feldern ermittelt, welche den Weg in der Hierarchie beschreibt.
   *
   * @param pParentBean die ausgehende Bean
   * @param pChain      die Folge von Feldern (Weg zur tiefen Bean)
   * @return die tiefe Bean innerhalb der Parent-Bean
   */
  @NotNull
  public static IBean<?> resolveDeepBean(IBean<?> pParentBean, List<IField<?>> pChain)
  {
    for (IField<?> field : pChain)
    {
      if (!pParentBean.hasField(field))
        throw new RuntimeException("bad-field: " + field.getName());

      Object value = pParentBean.getValue(field);
      assert value instanceof IBean;
      pParentBean = (IBean<?>) value;
    }

    return pParentBean;
  }

  /**
   * Ermittelt den Wert eines Bean-Feldes, welches sich tief in der Hierarchie befindet. (Über ein bis mehrere Bean-Felder)
   *
   * @param pParentBean die ausgehende Bean
   * @param pDeepField  das Feld, für welches der Wert gesucht ist
   * @param pChain      die Kette von Bean-Feldern, welche zu dem gesuchten Feld führen
   * @param <TYPE>      der Datentyp des tiefen Feldes
   * @return der Wert zu dem tiefen Feld
   */
  @Nullable
  public static <TYPE> TYPE resolveDeepValue(IBean<?> pParentBean, IField<TYPE> pDeepField, List<IField<?>> pChain)
  {
    IBean<?> deepBean = resolveDeepBean(pParentBean, pChain);

    if (!deepBean.hasField(pDeepField))
      throw new RuntimeException("deepField: " + pDeepField.getName());

    return deepBean.getValue(pDeepField);
  }
}
