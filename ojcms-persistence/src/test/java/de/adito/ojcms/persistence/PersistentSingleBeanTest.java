package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.cdi.AbstractCdiTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for injectable and persistent {@link IBean} instances.
 * The content is based on {@link BeanLoaderForTest} and {@link BeanStorageForTest}.
 *
 * @author Simon Danner, 10.01.2020
 */
public class PersistentSingleBeanTest extends AbstractCdiTest
{
  @Inject
  private SomeSingleBean bean;
  @Inject
  private SomeSingleBean bean2;

  @Test
  public void testFieldAdaptionForbidden()
  {
    assertThrows(UnsupportedOperationException.class,
                 () -> bean.fieldAdder(ShortField.class, "NEW_FIELD", Collections.emptySet()).addAtIndex(0));
    assertThrows(UnsupportedOperationException.class, () -> bean.removeField(SomeSingleBean.TEXT_FIELD));
  }

  @Test
  public void testSetAndGetValue()
  {
    final Instant timestamp = Instant.now();
    bean.setValue(SomeSingleBean.NUMBER_FIELD, 42);
    bean.setValue(SomeSingleBean.DATE_FIELD, timestamp);
    assertEquals(42, bean.getValue(SomeSingleBean.NUMBER_FIELD));
    assertEquals(timestamp, bean.getValue(SomeSingleBean.DATE_FIELD));

    //Test if the same bean, but injected twice, acts as one
    bean2.setValue(SomeSingleBean.NUMBER_FIELD, 41);
    assertEquals(41, bean.getValue(SomeSingleBean.NUMBER_FIELD));
  }

  @Persist(containerId = "SINGLE_TEST", mode = EPersistenceMode.SINGLE)
  public static class SomeSingleBean extends OJBean<SomeSingleBean> implements IModifiableBean<SomeSingleBean>
  {
    public static final IntegerField NUMBER_FIELD = OJFields.create(SomeSingleBean.class);
    public static final DateField DATE_FIELD = OJFields.create(SomeSingleBean.class);
    public static final TextField TEXT_FIELD = OJFields.create(SomeSingleBean.class);
  }
}
