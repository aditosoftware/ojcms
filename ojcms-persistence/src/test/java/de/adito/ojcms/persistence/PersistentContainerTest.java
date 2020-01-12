package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.cdi.AbstractCdiTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;

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

  @Persist(containerId = "TEST")
  public static class SomeBean extends OJBean<SomeBean>
  {
    @Identifier
    public static final IntegerField NUMBER_FIELD = OJFields.create(SomeBean.class);
    public static final DateField DATE_FIELD = OJFields.create(SomeBean.class);
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
}
