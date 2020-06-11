package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.cdi.AbstractCdiTest;
import de.adito.ojcms.transactions.annotations.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for an injectable persistent {@link IBeanContainer} for a base bean type.
 * The content is based on {@link BeanLoaderForTest} and {@link BeanStorageForTest}.
 *
 * @author Simon Danner, 20.03.2020
 */
public class PersistentBaseContainerTest extends AbstractCdiTest
{
  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private IBeanContainer<SomeBaseBean> baseContainer;

  @Test
  public void testAddBean()
  {
    baseContainer.addBean(new SomeConcreteBean(1, 1L, true, "1"));
    baseContainer.addBean(new SomeConcreteBean(2, 2L, false, "2"));
    baseContainer.addBean(new SomeOtherConcreteBean(3, 3L, true, "3"));
    baseContainer.addBean(new SomeOtherConcreteBean(4, 4L, false, "4"));

    assertEquals(4, baseContainer.size());
    assertSame(SomeConcreteBean.class, baseContainer.getBean(0).getClass());
    assertSame(SomeOtherConcreteBean.class, baseContainer.getBean(2).getClass());
  }

  @Test
  public void testBaseContainerWithPresetContent()
  {
    final TransactionalContainers transactionalContainers = cdiControl.createInjected(TransactionalContainers.class);

    //Add some beans in first transaction
    transactionalContainers.doInNewTransaction(pContainer ->
    {
      pContainer.addBean(new SomeConcreteBean(1, 1L, true, "1"));
      pContainer.addBean(new SomeConcreteBean(2, 2L, false, "2"));
      pContainer.addBean(new SomeOtherConcreteBean(3, 3L, true, "3"));
    });

    //Modify container in second transaction
    transactionalContainers.doInNewTransaction(pContainer ->
    {
      //Remove one bean
      final SomeBaseBean removedBean = pContainer.removeBean(1);
      assertEquals(2, removedBean.getValue(SomeBaseBean.FIELD1));
      assertEquals(1, pContainer.getBean(0).getValue(SomeBaseBean.FIELD1));
      assertEquals(3, pContainer.getBean(1).getValue(SomeBaseBean.FIELD1));
    });
  }

  @PersistAsBaseType(containerId = "SOME_CONTAINER", forSubTypes = {SomeConcreteBean.class, SomeOtherConcreteBean.class})
  public static abstract class SomeBaseBean extends OJBean
  {
    @Identifier
    public static final IntegerField FIELD1 = OJFields.create(SomeBaseBean.class);
    @Identifier
    public static final LongField FIELD2 = OJFields.create(SomeBaseBean.class);
    public static final BooleanField FIELD3 = OJFields.create(SomeBaseBean.class);

    SomeBaseBean(int pValue1, long pValue2, boolean pValue3)
    {
      setValue(FIELD1, pValue1);
      setValue(FIELD2, pValue2);
      setValue(FIELD3, pValue3);
    }

    private SomeBaseBean()
    {
    }
  }

  public static class SomeConcreteBean extends SomeBaseBean
  {
    public static final TextField FIELD4 = OJFields.create(SomeConcreteBean.class);

    SomeConcreteBean(int pValue1, long pValue2, boolean pValue3, String pValue4)
    {
      super(pValue1, pValue2, pValue3);
      setValue(FIELD4, pValue4);
    }

    @SuppressWarnings("unused")
    private SomeConcreteBean()
    {
    }
  }

  public static class SomeOtherConcreteBean extends SomeBaseBean
  {
    public static final TextField FIELD5 = OJFields.create(SomeOtherConcreteBean.class);
    public static final TimestampField FIELD6 = OJFields.create(SomeOtherConcreteBean.class);

    SomeOtherConcreteBean(int pValue1, long pValue2, boolean pValue3, String pValue5)
    {
      super(pValue1, pValue2, pValue3);
      setValue(FIELD5, pValue5);
      setValue(FIELD6, Instant.now());
    }

    @SuppressWarnings("unused")
    private SomeOtherConcreteBean()
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
    private IBeanContainer<SomeBaseBean> transactionalContainer;

    @Transactional
    void doInNewTransaction(Consumer<IBeanContainer<SomeBaseBean>> pContainerBasedAction)
    {
      pContainerBasedAction.accept(transactionalContainer);
    }
  }
}
