package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.util.IBeanFieldPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Statische Factory, welche einen Bean-Listener für die Beans in einem Container liefert.
 * Der Listener informiert bei Änderungen alle Listener des Containers.
 * Dabei werden die Listener für die gleichen Container gecached.
 *
 * @author s.danner, 31.01.2017
 */
final class BeanListenerUtil
{
  private static final Map<IBeanContainer, IBeanChangeListener> LISTENER_CACHE = new WeakHashMap<>();

  private BeanListenerUtil()
  {
  }

  /**
   * Setzt den Wert eines Feldes einer Bean und gibt gleichzeitig den Listenern Bescheid.
   * Hier passiert alles auf der Ebene des Datenkerns. Optionale Felder sind daher nicht mehr von Bedeutung.
   *
   * @param pBean     die Bean, zu welcher das Feld gehört
   * @param pField    das Feld, von welchem der Datenwert geändert wurde
   * @param pNewValue der neue Wert
   * @param <BEAN>    der generische Typ der Bean, für welche der Wert gesetzt wird
   * @param <TYPE>    der generische Typ des Datenwertes des Bean-Feldes
   */
  public static <BEAN extends IBean<BEAN>, TYPE> void setValueAndFire(BEAN pBean, IField<TYPE> pField, TYPE pNewValue)
  {
    IBeanEncapsulated<BEAN> encapsulated = pBean.getEncapsulated();
    assert encapsulated != null;
    assert encapsulated.containsField(pField);
    TYPE oldValue = encapsulated.getValue(pField);
    if (!Objects.equals(oldValue, pNewValue))
    {
      IBeanFieldActivePredicate<BEAN> fieldActiveSupplier = pBean.getFieldActiveSupplier();
      //Vorher aktive optionale Felder speichern
      List<IField<?>> optionalActiveFields = encapsulated.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .collect(Collectors.toList());
      //Wert anschließend setzen
      encapsulated.setValue(pField, pNewValue);
      //Nun feststellen, ob sich die Aktivitäts-Zustände der optionalen Felder verändert haben
      encapsulated.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .filter(pActiveField -> !optionalActiveFields.remove(pActiveField))
          .forEach(pNewActiveField -> encapsulated.fire(pListener -> pListener.fieldAdded(pBean, pNewActiveField)));
      //Übrige als removed feuern
      //noinspection unchecked
      optionalActiveFields.stream()
          .map(pBeforeActiveField -> (IField) pBeforeActiveField)
          .forEach(pBeforeActiveField -> encapsulated.fire(pListener -> pListener.fieldRemoved(pBean, pBeforeActiveField,
                                                                                               encapsulated.getValue(pBeforeActiveField))));
      //WICHTIG: Wertänderung erst am Schluss feuern, damit bereits alle Felder vorhanden sind
      encapsulated.fire(pListener -> pListener.beanChanged(pBean, pField, oldValue));
      //Referenzen noch anpassen, falls nötig
      if (pField instanceof IHierarchicalField)
      {
        IHierarchicalField<TYPE> field = (IHierarchicalField<TYPE>) pField;
        //Alte Referenzen entfernen, anhand des vorherigen Wertes
        field.getReferables(oldValue)
            .forEach(pReferable -> pReferable.removeReference(pBean, field));
        //Neue Referenzen hinzufügen
        field.getReferables(pNewValue)
            .forEach(pReferable -> pReferable.addWeakReference(pBean, field));
      }
    }
  }

  /**
   * Fügt der hinzugefügten Bean einen internen Listener hinzu und informiert die bereits registrierten Listener des Containers
   *
   * @param pContainer der Container, wo die Bean hinzugefügt wurde
   * @param pBean      die neue Bean
   * @param <BEAN>     der generische Typ der Bean, welche hinzugefügt wurde
   */
  public static <BEAN extends IBean<BEAN>> void beanAdded(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    pBean.listenWeak(_getListener(pContainer));
    pContainer.getEncapsulated().fire(pListener -> pListener.beanAdded(pBean));
    //Referenz auf die Bean, bei allen Bean-Referenzen des Containers eintragen
    pContainer.getEncapsulated().getHierarchicalStructure().getDirectParents()
        .forEach(pNode -> pBean.getEncapsulated().addWeakReference(pNode.getBean(), pNode.getField()));
  }

  /**
   * De-registriert den internen Listener der entfernten Bean und informiert die bereits registrierten Listener des Containers
   *
   * @param pContainer der Container, wo die Bean enfernt wurde
   * @param pBean      die entfernte Bean
   * @param <BEAN>     der generische Typ der Bean, welche entfernt wurde
   */
  public static <BEAN extends IBean<BEAN>> void beanRemoved(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    pBean.unlisten(_getListener(pContainer));
    pContainer.getEncapsulated().fire(pListener -> pListener.beanRemoved(pBean));
    //Referenz entfernen, wenn Container von einem Bean-Feld referenziert wird
    pBean.getHierarchicalStructure().getDirectParents().stream()
        .filter(pNode -> pNode.getBean().getValue(pNode.getField()) == pContainer) //Gibt es Parents, die diesen Container referenzieren?
        .forEach(pNode -> pBean.getEncapsulated().removeReference(pNode.getBean(), pNode.getField()));
  }

  /**
   * Wandelt eine kopierte Bean in eine Bean um, welche mitbekommt, wenn sich an der Original-Bean etwas verändert.
   * Dabei wird ein Listener angefügt, welcher solange 'weak' gehalten wird, bis die kopierte Bean nicht mehr referenziert wird.
   *
   * @param pOriginal       die Original-Bean
   * @param pCopy           die kopierte Bean
   * @param pIsFlat         <tt>true</tt>, wenn die Kopie geflattet wurde
   * @param pFieldPredicate ein optionales Feld-Prädikat, womit Felder gefiltert werden sollen
   * @return die umgewandelte und später informierte Bean
   */
  public static IBean<?> makeChangeAware(IBean<?> pOriginal, IBean pCopy, boolean pIsFlat, @Nullable IBeanFieldPredicate pFieldPredicate)
  {
    //noinspection unchecked
    return new ChangeAwareBean<>(pOriginal, pCopy, pIsFlat, pFieldPredicate);
  }

  /**
   * Liefert den Bean-Listener für einen Container.
   *
   * @param pContainer der Bean-Container
   * @param <BEAN>     der Typ der Beans, welche in dem Container enthalten sind
   * @return der Listener für den Container
   */
  private static <BEAN extends IBean<BEAN>> IBeanChangeListener<BEAN> _getListener(IBeanContainer<BEAN> pContainer)
  {
    //noinspection unchecked
    return LISTENER_CACHE.computeIfAbsent(pContainer, pKey -> new _Listener(pContainer.getEncapsulated().getWeakListeners()));
  }

  /**
   * Der Bean-Change-Listener für Beans in einem Container, welcher bei Änderung die Listener des Containers informiert.
   *
   * @param <BEAN> der Typ der Beans, welche in dem Container enthalten sind
   */
  private static class _Listener<BEAN extends IBean<BEAN>> implements IBeanChangeListener<BEAN>
  {
    private final List<IBeanContainerChangeListener<BEAN>> containerListeners;

    public _Listener(List<IBeanContainerChangeListener<BEAN>> pContainerListeners)
    {
      containerListeners = pContainerListeners;
    }

    @Override
    public <TYPE> void beanChanged(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
    {
      _fire(pListener -> pListener.beanChanged(pBean, pField, pOldValue));
    }

    @Override
    public <TYPE> void fieldAdded(BEAN pBean, IField<TYPE> pField)
    {
      _fire(pListener -> pListener.fieldAdded(pBean, pField));
    }

    @Override
    public <TYPE> void fieldRemoved(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
    {
      _fire(pListener -> pListener.fieldRemoved(pBean, pField, pOldValue));
    }

    /**
     * Gibt das Event an die Container-Listener weiter
     *
     * @param pAction die Aktion die weitergegeben werden soll
     */
    private void _fire(Consumer<IBeanContainerChangeListener<BEAN>> pAction)
    {
      synchronized (containerListeners)
      {
        containerListeners.forEach(pAction);
      }
    }
  }
}
