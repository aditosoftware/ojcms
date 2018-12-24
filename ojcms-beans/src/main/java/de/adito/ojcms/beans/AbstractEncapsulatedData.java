package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IDataSource;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.reactive.IEvent;
import de.adito.ojcms.beans.references.BeanReference;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract implementation of an encapsulated data core.
 * Handles weak references and change events.
 *
 * @param <ELEMENT>    the type of the elements in the data core
 * @param <DATASOURCE> the type of the data source of the data core
 * @author Simon Danner, 24.11.2018
 */
@EncapsulatedData
abstract class AbstractEncapsulatedData<ELEMENT, DATASOURCE extends IDataSource> implements IEncapsulatedData<ELEMENT, DATASOURCE>
{
  private final Map<IEncapsulatedBeanData, Set<BeanReference>> weakReferencesMapping =
      Collections.synchronizedMap(new WeakHashMap<>());
  private final Map<Class<? extends IEvent>, PublishSubject<? extends IEvent>> eventSubjects = new ConcurrentHashMap<>();
  private DATASOURCE datasource;

  /**
   * Creates the encapsulated data core with an initial data source.
   *
   * @param pDataSource the initial data source
   */
  AbstractEncapsulatedData(DATASOURCE pDataSource)
  {
    setDataSource(pDataSource);
  }

  @Override
  public void setDataSource(@NotNull DATASOURCE pDataSource)
  {
    datasource = Objects.requireNonNull(pDataSource);
  }

  @Override
  public Set<BeanReference> getDirectReferences()
  {
    return weakReferencesMapping.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public void addWeakReference(IBean<?> pBean, IField<?> pField)
  {
    weakReferencesMapping.computeIfAbsent(pBean.getEncapsulatedData(), pKey -> new HashSet<>()).add(new BeanReference(pBean, pField));
  }

  @Override
  public void removeReference(IBean<?> pBean, IField<?> pField)
  {
    final IEncapsulatedBeanData encapsulatedData = pBean.getEncapsulatedData();
    boolean removed = false;
    if (weakReferencesMapping.containsKey(encapsulatedData))
    {
      final Set<BeanReference> references = weakReferencesMapping.get(encapsulatedData);
      final Iterator<BeanReference> it = references.iterator();
      while (it.hasNext())
      {
        final BeanReference reference = it.next();
        if (reference != null && reference.getField() == pField)
        {
          it.remove();
          removed = true;
          if (references.isEmpty())
            weakReferencesMapping.remove(encapsulatedData);
          break;
        }
      }
    }
    if (!removed)
      throw new OJInternalException("Unable to remove reference! bean: " + pBean + " field: " + pField);
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
   * The data source this data core is based on.
   *
   * @return the data source
   */
  DATASOURCE getDatasource()
  {
    return datasource;
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
