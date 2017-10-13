package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.util.WeakArrayList;

import java.util.*;

/**
 * Behälter für grundlegende Daten eines Bean-Kerns (Bean oder Container)
 * Dient hier als Ersatz für eine abstrakte Basis-Klasse.
 *
 * @param <BEAN>     der generische Typ der einzelnen Bean oder der Beans in einem Container
 * @param <LISTENER> der Typ der Bean-Listeners, welche hier verwaltet werden
 * @author s.danner, 04.09.2017
 */
class BeanBaseData<BEAN extends IBean<BEAN>, LISTENER extends IBeanChangeListener<BEAN>>
{
  private final List<LISTENER> listenerContainer = new WeakArrayList<>();
  private final Set<ITransformable> linkContainer = Collections.newSetFromMap(new WeakHashMap<>());
  private final Map<IBean<?>, Set<IHierarchicalField<?>>> references = new WeakHashMap<>();

  /**
   * Liefert den Container der registrierten Listener.
   */
  public List<LISTENER> getWeakListenerContainer()
  {
    return listenerContainer;
  }

  /**
   * Liefert den Container der Links (transformierte Komponenten).
   */
  public Set<ITransformable> getWeakLinkContainer()
  {
    return linkContainer;
  }

  /**
   * Liefert die Map der Referenzen des Kerns.
   */
  public Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferences()
  {
    return references;
  }
}
