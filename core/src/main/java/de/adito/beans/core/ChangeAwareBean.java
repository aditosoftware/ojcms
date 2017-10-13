package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.util.BeanUtil;
import de.adito.beans.core.util.IBeanFieldPredicate;
import de.adito.beans.core.util.exceptions.BeanFlattenException;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.*;

/**
 * Wrapper-Klasse für eine beliebige Bean, welche als Listener fungiert,
 * welcher Änderungen an einer Original-Bean zu dieser Bean weiterleitet.
 *
 * @param <BEAN> der konkrete Typ der Bean, welche hier umhüllt wird
 * @author s.danner, 12.06.2017
 */
class ChangeAwareBean<BEAN extends IBean<BEAN>> implements IModifiableBean<BEAN>, IBeanChangeListener
{
  //Hier das Original halten, um die Referenzen bei mehreren aufeinanderfolgenden Kopien nicht zu verlieren (Der Listener ist nur weak)
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final IBean<?> original;
  private final BEAN source;

  private final boolean isFlat;
  private final IBeanFieldPredicate fieldPredicate;

  /**
   * Erzeugt die Proxy-Bean, welche mit dem Listener ausgestattet ist.
   *
   * @param pOriginal       die Original-Bean, auf welche gehört wird
   * @param pSource         die Quell-Bean
   * @param pFieldPredicate ein optionales Feld-Prädikat, womit Felder gefiltert werden sollen
   */
  public ChangeAwareBean(IBean<?> pOriginal, BEAN pSource, boolean pIsFlat, @Nullable IBeanFieldPredicate pFieldPredicate)
  {
    assert pSource.getEncapsulated() != null;
    original = pOriginal;
    source = pSource;
    isFlat = pIsFlat;
    fieldPredicate = pFieldPredicate;
    //noinspection unchecked
    original.listenWeak(this);
  }

  @Override
  public IBeanEncapsulated<BEAN> getEncapsulated()
  {
    return source.getEncapsulated();
  }

  @Override
  public IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    IBeanFieldActivePredicate<BEAN> fieldActiveSupplier = IModifiableBean.super.getFieldActiveSupplier();

    return new IBeanFieldActivePredicate<BEAN>()
    {
      @Override
      public BEAN getBean()
      {
        return source.getFieldActiveSupplier().getBean();
      }

      @Override
      public boolean isOptionalActive(IField<?> pField)
      {
        //Nur als aktiv markieren, wenn auch das Prädikat zutrifft
        return _checkFieldPredicate(pField, getEncapsulated().getValue(pField)) && fieldActiveSupplier.isOptionalActive(pField);
      }
    };
  }

  @Override
  public Stream<IField<?>> streamFields()
  {
    return IModifiableBean.super.streamFields()
        .filter(pField -> _checkFieldPredicate(pField, source.getValue(pField)));
  }

  @Override
  public Stream<Map.Entry<IField<?>, Object>> stream()
  {
    return IModifiableBean.super.stream()
        .filter(pEntry -> _checkFieldPredicate(pEntry.getKey(), pEntry.getValue()));
  }

  @Override
  public void fieldAdded(IBean pBean, IField pField)
  {
    //noinspection unchecked
    addField(pField, _getIndexOfAddedField(pBean, pField));
  }

  @Override
  public void fieldRemoved(IBean pBean, IField pField, Object pOldValue)
  {
    //noinspection unchecked
    removeField(pField);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void beanChanged(IBean pBean, IField pField, Object pOldValue)
  {
    IBean bean = pBean;
    IField<?> field = pField;
    if (isFlat && !getEncapsulated().containsField(field)) //Hier über Datenkern, weil das Feld auch wegen des Feld-Prädikats nicht aktiv sein könnte
    {
      Object changedValue = pBean.getValue(pField);
      //Der Wert MUSS hier eine Bean sein, da nur Bean-Felder abgeflacht werden
      assert pOldValue instanceof IBean;
      assert changedValue instanceof IBean;
      bean = pBean.flatCopy(false);
      field = _findChangedField((IBean<BEAN>) pOldValue, (IBean<BEAN>) changedValue);
    }
    if (_checkFieldPredicate(field, bean.getValue(field)))
    {
      assert bean.hasField(field);
      BeanListenerUtil.setValueAndFire((IBean) this, (IField) field, bean.getValue(field));
    }
  }

  /**
   * Prüft, ob eine Feld einer Bean dem Feld-Prädikat entspricht, auf welchem diese Bean basiert.
   *
   * @param pField der Feld der Bean
   * @param pValue der aktuelle Wert des Feldes
   * @return <tt>true</tt>, wenn das Feld dem Prädikat entspricht
   */
  private boolean _checkFieldPredicate(IField<?> pField, Object pValue)
  {
    return fieldPredicate == null || fieldPredicate.test(pField, pValue);
  }

  /**
   * Liefert den Index eines Bean-Feldes, welches neu hinzugefügt wurde, für die Source-Bean (evtl. weniger Felder)
   *
   * @param pBean       die Original-Bean, zu welcher das Feld hinzugefügt wurde
   * @param pAddedField das hinzugefügte Feld
   * @return der Index innerhalb dieser/der Source-Bean
   */
  private int _getIndexOfAddedField(IBean<?> pBean, IField<?> pAddedField)
  {
    int originalIndex = pBean.getFieldIndex(pAddedField);
    if (fieldPredicate == null)
      return originalIndex;

    return (int) pBean.streamFields()
        .filter(pField -> pBean.getFieldIndex(pField) < originalIndex) //Alle mit zu großem Index und es selbst raus
        .filter(pField -> fieldPredicate.test(pField, pBean.getValue(pField))) //Alle raus, die nicht dem Prädikat entsprechen
        .count(); //Summe der übrigen muss somit der Index des neuen Feldes sein
  }


  /**
   * Sucht das Feld, an welches unterschiedliche Daten innerhalb zweier Beans besitzt.
   *
   * @param pOldBean die alte Bean
   * @param pNewBean die neue Bean
   * @return das Feld, bei welchem sich der Wert verändert hat
   */
  private IField<?> _findChangedField(IBean<BEAN> pOldBean, IBean<BEAN> pNewBean)
  {
    return BeanUtil.compareBeanValues(pOldBean, pNewBean, pOldBean.streamFields().collect(Collectors.toList()))
        .orElseThrow(BeanFlattenException::new);
  }
}
