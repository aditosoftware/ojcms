package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.base.AbstractOnNextCallCountTest;
import de.adito.ojcms.beans.exceptions.bean.BeanCreationNotObservableException;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static de.adito.ojcms.beans.BeanCreationEvents.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BeanCreationEvents}.
 *
 * @author Simon Danner, 27.12.2018
 */
public class BeanCreationEventsTest extends AbstractOnNextCallCountTest
{
  @Test
  public void testObserveByBeanType()
  {
    justCallCheck(observeCreationByBeanType(SomeBean.class), 1);
    new SomeBean();
    justCallCheck(observeCreationByBeanType(AnotherBean.class), 1);
    new AnotherBean();
    assertThrows(BeanCreationNotObservableException.class, () -> observeCreationByBeanType(BeanWithoutAnnotation.class));
  }

  @Test
  public void testObserveByAnnotationType()
  {
    justCallCheck(observeCreationByAnnotationType(ObserveCreation.class), 1);
    new SomeBean();
    justCallCheck(observeCreationByAnnotationType(SomeAnnotation.class), 1);
    new AnotherBean();
    assertThrows(BeanCreationNotObservableException.class, () -> observeCreationByAnnotationType(Private.class));
  }

  @Test
  public void testHasObservableAnnotation()
  {
    assertTrue(hasObservableAnnotation(SomeBean.class));
    assertTrue(hasObservableAnnotation(AnotherBean.class));
    assertFalse(hasObservableAnnotation(BeanWithoutAnnotation.class));
  }

  /**
   * A bean type using {@link ObserveCreation} directly.
   */
  @ObserveCreation
  public static class SomeBean extends OJBean<SomeBean>
  {
  }

  /**
   * A bean type using the annotation annotated by {@link ObserveCreation}.
   */
  @SomeAnnotation
  public static class AnotherBean extends OJBean<AnotherBean>
  {
  }

  /**
   * A regular bean without any annotation.
   */
  public static class BeanWithoutAnnotation extends OJBean<BeanWithoutAnnotation>
  {
  }

  /**
   * An annotation annotated by {@link ObserveCreation}. Should work as well.
   */
  @ObserveCreation
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface SomeAnnotation
  {
  }
}
