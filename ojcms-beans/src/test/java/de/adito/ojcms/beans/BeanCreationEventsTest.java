package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.exceptions.bean.BeanCreationNotObservableException;
import de.adito.ojcms.beans.literals.fields.types.IntegerField;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static de.adito.ojcms.beans.BeanCreationEvents.*;
import static de.adito.ojcms.beans.base.reactive.StaticReactiveUtil.observe;
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
    observe(observeCreationByBeanType(SomeBean.class)) //
        .assertCallCount(1) //
        .whenDoing(SomeBean::new); //

    observe(observeCreationByBeanType(AnotherBean.class)) //
        .assertCallCount(1) //
        .whenDoing(AnotherBean::new);

    assertThrows(BeanCreationNotObservableException.class, () -> observeCreationByBeanType(BeanWithoutAnnotation.class));
  }

  @Test
  public void testObserveByAnnotationType()
  {
    observe(observeCreationByAnnotationType(ObserveCreation.class)) //
        .assertCallCount(1) //
        .whenDoing(SomeBean::new);

    observe(observeCreationByAnnotationType(SomeAnnotation.class)) //
        .assertCallCount(1) //
        .whenDoing(AnotherBean::new);

    assertThrows(BeanCreationNotObservableException.class, () -> observeCreationByAnnotationType(Detail.class));
  }

  @Test
  public void testHasObservableAnnotation()
  {
    assertTrue(hasObservableAnnotation(SomeBean.class));
    assertTrue(hasObservableAnnotation(AnotherBean.class));
    assertFalse(hasObservableAnnotation(BeanWithoutAnnotation.class));
  }

  @Test
  public void testObserveCreationIsAfterBeanConstruction()
  {
    observe(observeCreationByBeanType(SomeBean.class)) //
        .assertOnEveryValue(pNewBean -> assertEquals(10, pNewBean.getValue(SomeBean.SOME_FIELD))) //
        .whenDoing(SomeBean::new);
  }

  /**
   * A bean type using {@link ObserveCreation} directly.
   */
  @ObserveCreation
  public static class SomeBean extends OJBean
  {
    public static final IntegerField SOME_FIELD = OJFields.create(SomeBean.class);

    public SomeBean()
    {
      setValue(SOME_FIELD, 10);
    }
  }

  /**
   * A bean type using the annotation annotated by {@link ObserveCreation}.
   */
  @SomeAnnotation
  public static class AnotherBean extends OJBean
  {
  }

  /**
   * A regular bean without any annotation.
   */
  public static class BeanWithoutAnnotation extends OJBean
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
