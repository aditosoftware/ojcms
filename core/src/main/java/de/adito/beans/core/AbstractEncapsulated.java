package de.adito.beans.core;

import de.adito.beans.core.annotations.internal.Encapsulated;
import de.adito.beans.core.fields.IField;
import de.adito.beans.core.reactive.IEvent;
import de.adito.beans.core.references.BeanReference;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract implementation of an encapsulated data core.
 * Handles transformation links, weak references and change events.
 *
 * @param <CORE> the type of the elements in the data core
 * @author Simon Danner, 24.11.2018
 */
@Encapsulated
abstract class AbstractEncapsulated<CORE> implements IEncapsulated<CORE>
{
  private final Set<ITransformable> weakTransformationLinks = Collections.newSetFromMap(new WeakHashMap<>());
  private final Map<IBean<?>, Set<IField<?>>> weakReferencesMapping = new WeakHashMap<>();
  private final Map<Class<? extends IEvent>, PublishSubject<? extends IEvent>> eventSubjects = new ConcurrentHashMap<>();

  @Override
  public <LINK extends ITransformable> boolean isLinked(LINK pComponent)
  {
    synchronized (weakTransformationLinks)
    {
      return weakTransformationLinks.contains(pComponent);
    }
  }

  @Override
  public <LINK extends ITransformable> void registerWeakLink(LINK pComponent)
  {
    synchronized (weakTransformationLinks)
    {
      weakTransformationLinks.add(pComponent);
    }
  }

  @Override
  public Set<BeanReference> getDirectReferences()
  {
    synchronized (weakReferencesMapping)
    {
      return weakReferencesMapping.entrySet().stream()
          .flatMap(pEntry -> pEntry.getValue().stream()
              .map(pField -> new BeanReference(pEntry.getKey(), pField)))
          .collect(Collectors.toSet());
    }
  }

  @Override
  public void addWeakReference(IBean<?> pBean, IField<?> pField)
  {
    synchronized (weakReferencesMapping)
    {
      weakReferencesMapping.computeIfAbsent(pBean, pKey -> new HashSet<>()).add(pField);
    }
  }

  @Override
  public void removeReference(IBean<?> pBean, IField<?> pField)
  {
    synchronized (weakReferencesMapping)
    {
      if (!weakReferencesMapping.containsKey(pBean))
        return;
      Set<IField<?>> fields = weakReferencesMapping.get(pBean);
      fields.remove(pField);
      if (fields.isEmpty())
        weakReferencesMapping.remove(pBean);
    }
  }

  @Override
  public <EVENT extends IEvent<?>> Observer<EVENT> getEventObserverFromType(Class<EVENT> pEventType)
  {
    return _getEventSubject(pEventType);
  }

  @Override
  public <EVENT extends IEvent<?>> Observable<EVENT> observeByType(Class<EVENT> pEventType)
  {
    return _getEventSubject(pEventType);
  }

  /**
   * Retrieves an {@link PublishSubject} for a certain event type.
   * For each event type exactly one subject will be created.
   *
   * @param pEventType the event type the get the {@link PublishSubject} for
   * @return the {@link PublishSubject} for the event type
   */
  private <EVENT extends IEvent<?>> PublishSubject<EVENT> _getEventSubject(Class<EVENT> pEventType)
  {
    //noinspection unchecked
    return (PublishSubject<EVENT>) eventSubjects.computeIfAbsent(pEventType, pType -> PublishSubject.create());
  }
}
