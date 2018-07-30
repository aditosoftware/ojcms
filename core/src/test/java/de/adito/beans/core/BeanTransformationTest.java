package de.adito.beans.core;

import de.adito.beans.core.fields.TextField;
import de.adito.beans.core.util.exceptions.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the whole transformation concept.
 * For now the tests only include bean transformation.
 * Container transformation is based on the same abstract implementation, so it can be assumed, it is tested as well.
 *
 * @author Simon Danner, 30.07.2018
 * @see ITransformable
 */
public class BeanTransformationTest
{
  private static final String FIRST_VALUE = "firstValue";
  private static final String SECOND_VALUE = "secondValue";

  @Test
  public void testTransformationToBean()
  {
    final SomeBean bean = new SomeBean();
    final _UIComponentMockup uiComponent = new _UIComponentMockup();
    uiComponent.transform(bean);
    _checkTransformation(bean, uiComponent);
  }

  @Test
  public void testFieldLinkage()
  {
    final String input = "input";
    final SomeBean bean = new SomeBean();
    final _UIComponentMockup uiComponent = new _UIComponentMockup();
    uiComponent.transform(bean);
    final _UISubComponentMockup subComponent = uiComponent.getTransformator().createLinkedVisualComponent(SomeBean.someField);
    subComponent.input(input);
    assertEquals(input, bean.getValue(SomeBean.someField));
    assertEquals(input, uiComponent.getValue(SomeBean.someField));
  }

  @Test
  public void testBeforeTransformationQueue()
  {
    final SomeBean bean = new SomeBean();
    final _UIComponentMockup uiComponent = new _UIComponentMockup();
    final AtomicInteger someNumber = new AtomicInteger();
    uiComponent.queueOperation(() -> someNumber.set(1));
    uiComponent.transform(bean);
    assertEquals(1, someNumber.get());
  }

  @Test
  public void testTooEarlyAccessFailure()
  {
    final _UIComponentMockup uiComponent = new _UIComponentMockup();
    assertThrows(NotTransformedException.class, () -> uiComponent.getValue(SomeBean.someField));
    assertThrows(NotTransformedException.class, uiComponent::getOriginalSource);
  }

  @Test
  public void testTooLateQueueFailure()
  {
    final SomeBean bean = new SomeBean();
    final _UIComponentMockup uiComponent = new _UIComponentMockup();
    uiComponent.transform(bean);
    assertThrows(AlreadyTransformedException.class, () -> uiComponent.queueOperation(System.out::println));
  }

  @Test
  public void testSelfTransformation()
  {
    final SomeBean bean = new SomeBean();
    final _UIComponentMockupSelf uiComponent = new _UIComponentMockupSelf();
    uiComponent.transform(bean);
    _checkTransformation(bean, uiComponent);
  }

  /**
   * Checks, if a transformation has been executed.
   *
   * @param pBean          the source of the transformation
   * @param pTransformable the transformed component
   */
  private void _checkTransformation(SomeBean pBean, ITransformableBean pTransformable)
  {
    assertTrue(pTransformable.isTransformed());
    assertTrue(pBean.getEncapsulated().isLinked(pTransformable));
    assertEquals(FIRST_VALUE, pTransformable.getValue(SomeBean.someField));
    assertEquals(SECOND_VALUE, pTransformable.getValue(SomeBean.anotherField));
  }

  /**
   * Mockup of a self transformable UI component.
   */
  private static class _UIComponentMockupSelf implements ISelfTransformableBean<SomeBean, _UIComponentMockupSelf>
  {
    private SomeBean original;

    @Override
    public SomeBean getOriginalSource()
    {
      return original;
    }

    @Override
    public void link(SomeBean pLogicComponent, _UIComponentMockupSelf pVisualComponent)
    {
      original = pLogicComponent;
    }
  }

  /**
   * Mockup of a transformable UI component.
   */
  private static class _UIComponentMockup implements ITransformableBean<TextField, _UISubComponentMockup, SomeBean>
  {
    private IVisualBeanTransformator<TextField, _UISubComponentMockup, SomeBean> transformator;

    @Override
    public IVisualBeanTransformator<TextField, _UISubComponentMockup, SomeBean> getTransformator()
    {
      return transformator == null ? (transformator = new _Transformator()) : transformator;
    }
  }

  /**
   * Mockup of a UI sub component (think of a text field).
   * Listeners may be added to get informed about value changes.
   */
  private static class _UISubComponentMockup
  {
    private final List<Consumer<String>> listeners = new ArrayList<>();
    private String value;

    public void listen(Consumer<String> pInputConsumer)
    {
      synchronized (listeners)
      {
        listeners.add(pInputConsumer);
      }
    }

    public void input(String pNewValue)
    {
      value = pNewValue;
      synchronized (listeners)
      {
        listeners.forEach(pListener -> pListener.accept(value));
      }
    }
  }

  /**
   * The transformator for transformation of the ui component.
   * Links the graphical components with the logic bean parts.
   */
  private static class _Transformator extends AbstractBeanVisualTransformator<TextField, _UISubComponentMockup, SomeBean>
  {
    @Override
    public _UISubComponentMockup createVisualComponent(TextField pLogicComponent)
    {
      return new _UISubComponentMockup();
    }

    @Override
    public void link(TextField pField, _UISubComponentMockup pVisualComponent)
    {
      pVisualComponent.listen(pInput -> getOriginalSource().setValue(pField, pInput));
    }
  }

  /**
   * Some bean.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final TextField someField = BeanFieldFactory.create(SomeBean.class);
    public static final TextField anotherField = BeanFieldFactory.create(SomeBean.class);

    public SomeBean()
    {
      setValue(someField, FIRST_VALUE);
      setValue(anotherField, SECOND_VALUE);
    }
  }
}
