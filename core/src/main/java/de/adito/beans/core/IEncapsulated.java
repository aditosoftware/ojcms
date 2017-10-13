package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.references.IHierarchicalStructure;
import de.adito.beans.core.references.IReferable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * Beschreibt den abgekapselten Daten-Kern innerhalb eines Bean-Elements.
 * Stellt die 'private'-Scope auf Java-Ebene dar.
 *
 * Wichtig: Dieses Interface muss package-protected bleiben, um die Datenkapselung zu gewähren.
 *
 * @param <CORE>     der Typ der Elemente, welche im Datenkern enthalten sind
 * @param <BEAN>     der generische Bean-Typ dieses Kerns
 * @param <LISTENER> der Typ der Bean-Listener, welche hier registriert werden können
 * @author s.danner, 25.01.2017
 */
interface IEncapsulated<CORE, BEAN extends IBean<BEAN>, LISTENER extends IBeanChangeListener<BEAN>> extends Iterable<CORE>, ILinkable, IReferable
{
  /**
   * Liefert die grundlegenden Daten dieses Kerns (Ersatz für abstrakte Klasse).
   *
   * @return eine Objekt mit grundlegenden Daten für Listener etc.
   */
  BeanBaseData<BEAN, LISTENER> getBeanBaseData();

  /**
   * Liefert die Menge der registrierten Listener. (muss schwach sein)
   */
  default List<LISTENER> getWeakListeners()
  {
    assert getBeanBaseData() != null;
    return getBeanBaseData().getWeakListenerContainer();
  }

  /**
   * Liefert den konkreten Container der verlinkten transformierbaren Komponenten.
   */
  default Collection<ITransformable> getWeakLinkedContainer()
  {
    assert getBeanBaseData() != null;
    return getBeanBaseData().getWeakLinkContainer();
  }

  @Override
  default Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferenceMap()
  {
    assert getBeanBaseData() != null;
    return getBeanBaseData().getWeakReferences();
  }

  /**
   * Fügt dem Kern einen neuen Listener hinzu.
   *
   * @param pListener der neue Listener
   */
  default void addListener(LISTENER pListener)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      getWeakListeners().add(pListener);
    }
  }

  /**
   * Entfernt einen Listener.
   *
   * @param pListener der zu entfernende Listener
   */
  default void removeListener(LISTENER pListener)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      getWeakListeners().remove(pListener);
    }
  }

  /**
   * Löst eine Listener-Aktion bei allen registrierten aus.
   *
   * @param pAction die Aktion, welche ausgeführt werden soll
   */
  default void fire(Consumer<LISTENER> pAction)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      getWeakListeners().forEach(pAction);
    }
  }

  @Override
  default <LINK extends ITransformable> boolean isLinked(LINK pComponent)
  {
    assert getWeakLinkedContainer() != null;
    return getWeakLinkedContainer().contains(pComponent);
  }

  @Override
  default <LINK extends ITransformable> void registerWeakLink(LINK pComponent)
  {
    assert getWeakLinkedContainer() != null;
    getWeakLinkedContainer().add(pComponent);
  }

  /**
   * Liefert die hierarchische Struktur dieses Kerns.
   * Diese beinhaltet alle Referenzen auf die Hülle dieses Kerns.
   *
   * @return die Schnittstelle zum Abfragen von Informationen der Struktur
   */
  default IHierarchicalStructure getHierarchicalStructure()
  {
    return new HierarchicalStructureImpl<>(this);
  }

  /**
   * Liefert einen Stream der Elemente, welche in diesem Kern enthalten sind
   */
  default Stream<CORE> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Liefert einen parallelen Stream der Elemente, welche in diesem Kern enthalten sind
   */
  default Stream<CORE> parallelStream()
  {
    return StreamSupport.stream(spliterator(), true);
  }

  /**
   * Default-Implementierung für die hierarchische Struktur.
   */
  class HierarchicalStructureImpl<C, B extends IBean<B>, L extends IBeanChangeListener<B>> implements IHierarchicalStructure
  {
    private final IEncapsulated<C, B, L> encapsulated;

    public HierarchicalStructureImpl(IEncapsulated<C, B, L> pEncapsulated)
    {
      encapsulated = pEncapsulated;
    }

    @Override
    public void destroy()
    {
      encapsulated.getWeakReferenceMap().clear();
    }

    @Override
    public Set<IHierarchicalNode> getDirectParents()
    {
      return encapsulated.getWeakReferenceMap().entrySet().stream()
          .flatMap(pEntry -> pEntry.getValue().stream()
              .map(pField -> new HierarchicalNodeImpl(pEntry.getKey(), pField)))
          .collect(Collectors.toSet());
    }
  }
}
