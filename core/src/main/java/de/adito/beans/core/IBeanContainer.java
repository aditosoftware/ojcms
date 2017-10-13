package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.BeanContainerListProxy;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Beschreibt die Hülle eines Bean-Containers.
 * Ein Bean-Container besteht aus einem abgekapselten Daten-Kern und der Hülle, welche über default-Methoden sämtliche Funktionalität bereitstellt.
 * Hier wird bewusst ein Interface als Hülle verwendet, um beliebige Objekte als Bean-Container behandeln zu können.
 *
 * Der Daten-Kern wird über die einzig nicht default-Methode geliefert.
 * Diese Methode kann auch als 'virtual-field' bezeichnet werden, da sich alle default-Methoden so Zugriff auf den Kern verschaffen.
 * Da der Daten-Kern gekapselt werden soll und Interfaces nur public-Methoden besitzen können, ist der Datenkern package-protected und
 * somit nur von 'innen' zugänglich.
 *
 * @param <BEAN> der Typ der Beans, welche dieser Container beinhaltet
 * @author s.danner, 23.08.2016
 */
public interface IBeanContainer<BEAN extends IBean<BEAN>> extends IEncapsulatedHolder<IBeanContainerEncapsulated<BEAN>>
{
  /**
   * Liefert den Typen der Beans, welche dieser Container beinhaltet.
   */
  default Class<BEAN> getBeanType()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getBeanType();
  }

  /**
   * Fügt dem Container einen Bean hinzu.
   * Dabei werden die Listener darüber benachrichtigt.
   *
   * @param pBean der neue Bean
   */
  default void addBean(BEAN pBean)
  {
    addBean(pBean, size());
  }

  /**
   * Fügt dem Container einen Bean an einer bestimmten Stelle hinzu.
   * Dabei werden die Listener darüber benachrichtigt.
   *
   * @param pBean  der neue Bean
   * @param pIndex die Stelle, wo der Bean eingefügt werden soll
   */
  default void addBean(BEAN pBean, int pIndex)
  {
    assert getEncapsulated() != null;
    getEncapsulated().add(pIndex, pBean);
    BeanListenerUtil.beanAdded(this, pBean);
  }

  /**
   * Ersetzt einen Bean an einer bestimmten Stelle.
   * Dabei werden die Listener darüber benachrichtigt.
   *
   * @param pBean  der neue Bean
   * @param pIndex die Stelle, die ersetzt werden soll
   * @return der ersetzte Bean
   */
  default BEAN replaceBean(BEAN pBean, int pIndex)
  {
    assert getEncapsulated() != null;
    BEAN removed = getEncapsulated().set(pIndex, pBean);
    if (removed != null)
      BeanListenerUtil.beanRemoved(this, removed);
    BeanListenerUtil.beanAdded(this, pBean);
    return removed;
  }

  /**
   * Entfernt einen Bean aus dem Container.
   * Dabei werden die Listener darüber benachrichtigt.
   *
   * @param pBean der Bean, welcher entfernt werden soll
   * @return <tt>true</tt>, wenn der Bean entfernt wurde
   */
  default boolean removeBean(BEAN pBean)
  {
    IBeanContainerEncapsulated<BEAN> enc = getEncapsulated();
    assert enc != null;
    boolean removed = enc.remove(pBean);
    if (removed)
      BeanListenerUtil.beanRemoved(this, pBean);
    return removed;
  }

  /**
   * Entfernt alle Beans, welche dem Prädikat entsprechen aus dem Container.
   *
   * @param pPredicate das Prädikat, welches bestimmt, welche Beans entfernt werden sollen
   * @return <tt>true</tt>, wenn mindestens eine Bean entfernt wurde
   */
  default boolean removeBeanIf(Predicate<BEAN> pPredicate)
  {
    assert getEncapsulated() != null;
    Iterator<BEAN> it = getEncapsulated().iterator();
    boolean removed = false;
    while (it.hasNext())
    {
      BEAN bean = it.next();
      if (pPredicate.test(bean))
      {
        it.remove();
        BeanListenerUtil.beanRemoved(this, bean);
        removed = true;
      }
    }
    return removed;
  }

  /**
   * Liefert den Bean an einer bestimmten Stelle.
   *
   * @param pIndex die bestimmte Stelle
   * @return der Bean an der Stelle
   */
  default BEAN getBean(int pIndex)
  {
    if (pIndex < 0)
      throw new RuntimeException("index: " + pIndex);

    assert getEncapsulated() != null;
    return getEncapsulated().get(pIndex);
  }

  /**
   * Liefert die Stelle, an der sich ein bestimmter Bean befindet.
   *
   * @param pBean der Bean, zu welchem die Stelle im Container ermittelt werden soll
   * @return die Stelle, an der sich der Bean befindet
   */
  default int indexOf(BEAN pBean)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().indexOf(pBean);
  }

  /**
   * Liefert die Anzahl der Beans, welche der Container enthält.
   */
  default int size()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().size();
  }

  /**
   * Leert den Bean-Container
   */
  default void clear()
  {
    assert getEncapsulated() != null;
    removeBeanIf(pBean -> true); //Nicht einfach den Kern clearen, da sonst die Listener nicht feuern
  }

  /**
   * Bestimmt, ob der Container einen bestimmten Bean enthält.
   *
   * @param pBean der bestimmte Bean
   * @return <tt>true</tt>, wenn der Bean enthalten ist
   */
  default boolean contains(BEAN pBean)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().contains(pBean);
  }

  /**
   * Legt eine maximale Anzahl an Beans für diesen Container fest.
   *
   * @param pMaxCount die maximale Anzahl an Beans (-1 für kein Limit)
   * @param pEvicting <tt>true</tt>, wenn die ältesten Beans entfernt werden sollen, wenn die Kapazität erreicht ist
   */
  default IBeanContainer<BEAN> withLimit(int pMaxCount, boolean pEvicting)
  {
    assert getEncapsulated() != null;
    getEncapsulated().setLimit(pMaxCount, pEvicting);
    return this;
  }

  /**
   * Registriert einen neuen Change-Listener für den Container.
   *
   * @param pChangeListener der neue Listener
   */
  default void listenWeak(IBeanContainerChangeListener<BEAN> pChangeListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addListener(pChangeListener);
  }

  /**
   * De-registriert einen Change-Listener.
   *
   * @param pChangeListener der zu entfernende Listener
   */
  default void unlisten(IBeanContainerChangeListener<BEAN> pChangeListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeListener(pChangeListener);
  }

  /**
   * Liefert die statistischen Daten des Containers. (Anzahl der Elemente über eine bestimmte Zeit)
   * null, wenn nicht vorhanden.
   */
  @Nullable
  default IStatisticData<Integer> getStatisticData()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getStatisticData();
  }

  /**
   * Liefert alle unterschiedlichen Werte eines Feldes.
   *
   * @param pField das Feld, für welches die unterschiedlichen Werte bestimmt werden sollen
   * @param <TYPE> der Datentyp des Feldes
   * @return ein Set mit den unterschiedlichen Datenwerten
   */
  default <TYPE> Set<TYPE> getDistinctValuesFromField(IField<TYPE> pField)
  {
    return getDistinctValues(pBean -> pBean.getValue(pField));
  }

  /**
   * Liefert alle unterschiedlichen Werte dieses Containers anhand eines beliebigen Value-Resolvers.
   * Der Resolver kann Werte nach unterschiedlichen Kriterien (Filtern) definieren.
   *
   * @param pValueResolver ein spezieller Resolver für den Wert
   * @param <TYPE>         der Datentyp des Feldes
   * @return ein Set mit den unterschiedlichen Datenwerten
   */
  default <TYPE> Set<TYPE> getDistinctValues(Function<BEAN, TYPE> pValueResolver)
  {
    return stream()
        .map(pValueResolver)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toSet());
  }

  /**
   * Wandelt den Container in einen List-Proxy um.
   *
   * @return eine List-Schnittstelle des Containers
   */
  default List<BEAN> toListProxy()
  {
    return new BeanContainerListProxy<>(this);
  }

  /**
   * Stellt diesen Container als Read-Only-Variante bereit.
   *
   * @return dieser Container als Read-Only-Variante
   */
  default IBeanContainer<BEAN> asReadOnly()
  {
    assert getEncapsulated() != null;
    return new ReadOnly<>(this);
  }

  /**
   * Liefert einen Stream von Beans, welche in diesem Container enthalten sind.
   */
  default Stream<BEAN> stream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().stream();
  }

  /**
   * Liefert einen parallelen Stream von Beans, welche in diesem Container enthalten sind.
   */
  default Stream<BEAN> parallelStream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().parallelStream();
  }

  /**
   * Führt eine bestimmte Aktion für jede Bean des Containers aus.
   *
   * @param pAction die Aktion, defniert als Consumer einer Bean
   */
  default void forEachBean(Consumer<BEAN> pAction)
  {
    stream().forEach(pAction);
  }

  /**
   * Read-Only Container-Implementierung.
   *
   * @param <BEAN> der Typ der Beans des Containers
   */
  class ReadOnly<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
  {
    private final IBeanContainer<BEAN> original;

    public ReadOnly(IBeanContainer<BEAN> pOriginal)
    {
      original = pOriginal;
    }

    @Override
    public IBeanContainerEncapsulated<BEAN> getEncapsulated()
    {
      return original.getEncapsulated();
    }

    @Override
    public void addBean(BEAN pBean) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addBean(BEAN pBean, int pIndex) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public BEAN replaceBean(BEAN pBean, int pIndex) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeBean(BEAN pBean) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeBeanIf(Predicate<BEAN> pPredicate) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException();
    }
  }
}
