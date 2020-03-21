package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.cdi.AbstractCdiTest;
import de.adito.ojcms.transactions.annotations.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for an injectable persistent {@link IBeanContainer}.
 * The content is based on {@link BeanLoaderForTest} and {@link BeanStorageForTest}.
 *
 * @author Simon Danner, 30.12.2019
 */
public class PersistentContainerTest extends AbstractCdiTest
{
  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private IBeanContainer<SomeBean> container;

  @Test
  public void testContainerSize()
  {
    assertTrue(container.isEmpty());

    container.addBean(new SomeBean(42));
    container.addBean(new SomeBean(12));
    container.addBean(new SomeBean(-10));

    assertEquals(3, container.size());
  }

  @Test
  public void testAddBean()
  {
    assertThrows(NullPointerException.class, () -> container.addBean(null));
    container.addBean(new SomeBean(42));
    assertEquals(1, container.size());
    assertEquals(42, container.getBean(0).getValue(SomeBean.NUMBER_FIELD));
    container.addBean(new SomeBean(12), 0);

    assertEquals(2, container.size());
    assertEquals(12, container.getBean(0).getValue(SomeBean.NUMBER_FIELD));
    assertEquals(42, container.getBean(1).getValue(SomeBean.NUMBER_FIELD));

    container.addMultiple(Arrays.asList(new SomeBean(1), new SomeBean(2)));
    assertEquals(4, container.size());
    assertEquals(1, container.getBean(2).getValue(SomeBean.NUMBER_FIELD));
    assertEquals(2, container.getBean(3).getValue(SomeBean.NUMBER_FIELD));
  }

  @Test
  public void testRemoveBean()
  {
    assertThrows(IndexOutOfBoundsException.class, () -> container.removeBean(0));

    container.addBean(new SomeBean(42));
    container.removeBean(0);
    assertTrue(container.isEmpty());

    container.addMultiple(Arrays.asList(new SomeBean(1), new SomeBean(2), new SomeBean(3)));
    container.removeBean(new SomeBean(2));
    assertEquals(2, container.size());
    assertEquals(1, container.getBean(0).getValue(SomeBean.NUMBER_FIELD));
    assertEquals(3, container.getBean(1).getValue(SomeBean.NUMBER_FIELD));
  }

  @Test
  public void testContainerWithPresetContent()
  {
    final TransactionalContainers transactionalContainers = cdiControl.createInjected(TransactionalContainers.class);

    //Add some beans in first transaction
    transactionalContainers.doInNewTransaction(pContainer ->
    {
      pContainer.addBean(new SomeBean(1));
      pContainer.addBean(new SomeBean(2));
      pContainer.addBean(new SomeBean(3));
    });

    //Modify container in second transaction
    transactionalContainers.doInNewTransaction(pContainer ->
    {
      //Remove one bean
      final SomeBean removedBean = pContainer.removeBean(1);
      assertEquals(2, removedBean.getValue(SomeBean.NUMBER_FIELD));
      assertEquals(1, pContainer.getBean(0).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(3, pContainer.getBean(1).getValue(SomeBean.NUMBER_FIELD));
      //Add some more bean at front
      pContainer.addBean(new SomeBean(99), 0);
      assertEquals(99, pContainer.getBean(0).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(1, pContainer.getBean(1).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(3, pContainer.getBean(2).getValue(SomeBean.NUMBER_FIELD));
      //Add another bean
      pContainer.addBean(new SomeBean(100), 1);
      assertEquals(4, pContainer.size());
      assertEquals(99, pContainer.getBean(0).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(100, pContainer.getBean(1).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(1, pContainer.getBean(2).getValue(SomeBean.NUMBER_FIELD));
      assertEquals(3, pContainer.getBean(3).getValue(SomeBean.NUMBER_FIELD));
    });
  }

  @Persist(containerId = "TEST")
  public static class SomeBean extends OJBean
  {
    @Identifier
    public static final IntegerField NUMBER_FIELD = OJFields.create(SomeBean.class);
    public static final TimestampField DATE_FIELD = OJFields.create(SomeBean.class);
    public static final TextField TEXT_FIELD = OJFields.create(SomeBean.class);

    public SomeBean(int pNumber)
    {
      setValue(NUMBER_FIELD, pNumber);
      setValue(DATE_FIELD, Instant.now());
      setValue(TEXT_FIELD, String.valueOf(pNumber));
    }

    /**
     * Required for persistent bean creation.
     */
    @SuppressWarnings("unused")
    private SomeBean()
    {
    }
  }

  /**
   * Allows to perform an action on a {@link IBeanContainer} within a newly started transaction.
   */
  @TransactionalScoped
  public static class TransactionalContainers
  {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private IBeanContainer<SomeBean> transactionalContainer;

    @Transactional
    void doInNewTransaction(Consumer<IBeanContainer<SomeBean>> pContainerBasedAction)
    {
      pContainerBasedAction.accept(transactionalContainer);
    }
  }
}
