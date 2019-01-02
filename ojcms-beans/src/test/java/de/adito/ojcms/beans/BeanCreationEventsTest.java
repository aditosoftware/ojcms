package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.exceptions.bean.BeanCreationNotObservableException;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static de.adito.ojcms.beans.BeanCreationEvents.*;
import static de.adito.ojcms.beans.base.reactive.StaticReactiveTest.observe;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BeanCreationEvents}.
 *
 * @author Simon Danner, 27.12.2018
 */
public class BeanCreationEventsTest
{
  @Test
  public void testObserveByBeanType()
  {
    observe(observeCreationByBeanType(SomeBean.class))
        .assertCallCount(1)
        .whenDoing(SomeBean::new);
    observe(observeCreationByBeanType(AnotherBean.class))
        .assertCallCount(1)
        .whenDoing(AnotherBean::new);
    assertThrows(BeanCreationNotObservableException.class, () -> observeCreationByBeanType(BeanWithoutAnnotation.class));
  }

  @Test
  public void testObserveByAnnotationType()
  {
    observe(observeCreationByAnnotationType(ObserveCreation.class))
        .assertCallCount(1)
        .whenDoing(SomeBean::new);
    observe(observeCreationByAnnotationType(SomeAnnotation.class))
        .assertCallCount(1)
        .whenDoing(AnotherBean::new);
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
