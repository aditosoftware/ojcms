package de.adito.beans.core;

import de.adito.beans.core.listener.*;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.util.IBeanFieldPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility methods concerning bean listeners.
 * They fire events, when a value of a bean was changed or a bean was added or removed to/from a container.
 * Furthermore this class provides a static listener cache for bean-listeners of a container (one listener per container).
 * This cache is used automatically by the utility methods.
 *
 * @author Simon Danner, 31.01.2017
 */
final class BeanListenerUtil
{
  private static final Map<IBeanContainer, IBeanChangeListener> LISTENER_CACHE = new WeakHashMap<>(); //weak cache!

  private BeanListenerUtil()
  {
  }

  /**
   * Sets the value for a bean field and informs all listeners about this change.
   * This may influence the active state of an optional field or create/remove a reference, which will be adjusted here.
   * This method only uses the encapsulated data core, so any special behaviour of optional fields, etc. won't matter.
   *
   * @param pBean     the bean, where a value has been changed
   * @param pField    the bean field from which the value has been changed
   * @param pNewValue the new value to set
   * @param <BEAN>    the generic bean type
   * @param <TYPE>    the data type of the bean field
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
      //Store before active optional fields to detect differences later on
      List<IField<?>> optionalActiveFields = encapsulated.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .collect(Collectors.toList());
      //Set the new value in the data core
      encapsulated.setValue(pField, pNewValue);
      //Find newly activated optional fields and fire them as added fields
      encapsulated.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .filter(pActiveField -> !optionalActiveFields.remove(pActiveField))
          .forEach(pNewActiveField -> encapsulated.fire(pListener -> pListener.fieldAdded(pBean, pNewActiveField)));
      //Fire the remaining as removed fields
      //noinspection unchecked
      optionalActiveFields.stream()
          .map(pBeforeActiveField -> (IField) pBeforeActiveField)
          .forEach(pBeforeActiveField -> encapsulated.fire(pListener -> pListener.fieldRemoved(pBean, pBeforeActiveField,
                                                                                               encapsulated.getValue(pBeforeActiveField))));
      //IMPORTANT: The value change itself must be fired after all optional fields have their correct active state
      encapsulated.fire(pListener -> pListener.beanChanged(pBean, pField, oldValue));
      //Finally adjust the references if necessary
      if (pField instanceof IHierarchicalField)
      {
        //A hierarchical field is able to provide all instances, which are referred by this field based on its set value
        IHierarchicalField<TYPE> field = (IHierarchicalField<TYPE>) pField;
        //Remove old references based on the old value
        field.getReferables(oldValue)
            .forEach(pReferable -> pReferable.removeReference(pBean, field));
        //Add the new ones
        field.getReferables(pNewValue)
            .forEach(pReferable -> pReferable.addWeakReference(pBean, field));
      }
    }
  }

  /**
   * A bean has been added to a container.
   * Adds a listener to the added bean and fires an addition event to the container listeners.
   * Furthermore registers the container references at the bean.
   *
   * @param pContainer the container, where the bean has been added
   * @param pBean      the added bean
   * @param <BEAN>     the generic type of the bean
   */
  public static <BEAN extends IBean<BEAN>> void beanAdded(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    pBean.listenWeak(_getListener(pContainer));
    pContainer.getEncapsulated().fire(pListener -> pListener.beanAdded(pBean));
    //Pass the references of the container to the beans as well
    pContainer.getEncapsulated().getHierarchicalStructure().getDirectParents()
        .forEach(pNode -> pBean.getEncapsulated().addWeakReference(pNode.getBean(), pNode.getField()));
  }

  /**
   * A bean has been removed from a container.
   * Removes the listener from the bean and fires an removal event to the container listeners.
   * Furthermore removes the container references from the bean.
   *
   * @param pContainer the container, where the bean has been removed
   * @param pBean      the removed bean
   * @param <BEAN>     the generic type of the bean
   */
  public static <BEAN extends IBean<BEAN>> void beanRemoved(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    pBean.unlisten(_getListener(pContainer));
    pContainer.getEncapsulated().fire(pListener -> pListener.beanRemoved(pBean));
    //Remove the references from the bean, which were created through the container
    pBean.getHierarchicalStructure().getDirectParents().stream()
        .filter(pNode -> pNode.getBean().getValue(pNode.getField()) == pContainer) //Filter the references to the affected container
        .forEach(pNode -> pBean.getEncapsulated().removeReference(pNode.getBean(), pNode.getField()));
  }

  /**
   * Converts a bean copy to a change aware bean to be able to get notified about changes at the original bean.
   *
   * @param pOriginal       the original bean
   * @param pCopy           the bean copy
   * @param pIsFlat         <tt>true</tt>, if the copy has flatted fields
   * @param pFieldPredicate an optional predicate, which maybe was used to exclude some fields from the copy
   * @return the change aware bean
   */
  public static IBean<?> makeChangeAware(IBean<?> pOriginal, IBean pCopy, boolean pIsFlat, @Nullable IBeanFieldPredicate pFieldPredicate)
  {
    //noinspection unchecked
    return new ChangeAwareBean<>(pOriginal, pCopy, pIsFlat, pFieldPredicate);
  }

  /**
   * Provides the single bean listener for a certain container.
   *
   * @param pContainer the bean container
   * @param <BEAN>     the type of the beans in the container
   * @return the bean listener for the container
   */
  private static <BEAN extends IBean<BEAN>> IBeanChangeListener<BEAN> _getListener(IBeanContainer<BEAN> pContainer)
  {
    //noinspection unchecked
    return LISTENER_CACHE.computeIfAbsent(pContainer, pKey -> new _Listener(pContainer.getEncapsulated().getWeakListeners()));
  }

  /**
   * A bean change listener for beans in a container, which forwards fire events to the listeners of the container.
   *
   * @param <BEAN> the type of the beans in the container
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
     * Forwards an event to the container's listeners.
     *
     * @param pAction a consumer for a bean change listener, which represents the fired event call
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