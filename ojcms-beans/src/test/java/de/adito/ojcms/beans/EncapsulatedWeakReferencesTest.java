package de.adito.ojcms.beans;

import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.fields.types.BeanField;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the weak reference store of a {@link IReferable}.
 *
 * @author Simon Danner, 22.12.2018
 */
public class EncapsulatedWeakReferencesTest
{
  private SomeBean someBean;
  private IReferable referable;

  @BeforeEach
  public void init()
  {
    referable = (someBean = new SomeBean()).getEncapsulatedData();
  }

  @Test
  public void testReferenceAddition()
  {
    assertTrue(referable.getDirectReferences().isEmpty());
    new ReferringBean(someBean);
    assertEquals(1, referable.getDirectReferences().size());
  }

  @Test
  public void testReferenceRemoval()
  {
    final ReferringBean referringBean = new ReferringBean(someBean);
    new ReferringBean(someBean); //create two references
    assertEquals(2, referable.getDirectReferences().size());
    referable.removeReference(referringBean, ReferringBean.refField);
    assertEquals(1, referable.getDirectReferences().size());
  }

  @Test
  public void testReferenceRemovalFail()
  {
    assertThrows(OJInternalException.class, () -> referable.removeReference(new ReferringBean(someBean), ReferringBean.anotherRefField));
  }

  @Test
  public void testWeakness() throws InterruptedException
  {
    //noinspection unused
    ReferringBean referringBean = new ReferringBean(someBean);
    assertFalse(referable.getDirectReferences().isEmpty());
    //noinspection UnusedAssignment
    referringBean = null;
    System.gc();
    Thread.sleep(100);
    assertTrue(referable.getDirectReferences().isEmpty());
  }

  /**
   * Some bean to take the encapsulated data core from to check the references.
   */
  public static class SomeBean extends OJBean<SomeBean>
  {
  }

  /**
   * Some bean that will establish a reference to {@link SomeBean}.
   */
  public static class ReferringBean extends OJBean<ReferringBean>
  {
    public static final BeanField<SomeBean> refField = OJFields.create(ReferringBean.class);
    public static final BeanField<SomeBean> anotherRefField = OJFields.create(ReferringBean.class);

    public ReferringBean(SomeBean pRefBean)
    {
      setValue(refField, pRefBean);
    }
  }
}
