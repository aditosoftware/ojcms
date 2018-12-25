package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.*;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

/**
 * Tests, if a warning is printed, if a generic bean field might be replaced by a concrete field type.
 *
 * @author Simon Danner, 24.12.2018
 */
public class GenericFieldReplacementTest
{
  private final Logger mockedLogger = mock(Logger.class);

  @BeforeEach
  public void setup()
  {
    GenericField.LOGGER = mockedLogger;
  }

  @Test
  public void testReplacementWarnings()
  {
    _checkLogging(Integer.class);
    _checkLogging(SomeBean.class);
    _checkLogging(BeanContainer.class);
    _checkLogging(String.class);
    _checkLogging(List.class);
    _checkLogging(TestEnum.class);
    _checkLogging(Date.class);
    _checkLogging(Instant.class);
    _checkLogging(Boolean.class);
    _checkLogging(Exception.class, false);
  }

  /**
   * Checks, if logging happened while creating a generic field of a certain type.
   *
   * @param pGenericType the generic data type of the field to create for the test
   */
  private void _checkLogging(Class<?> pGenericType)
  {
    _checkLogging(pGenericType, true);
  }

  /**
   * Checks, if logging happened or not while creating a generic field of a certain type.
   *
   * @param pGenericType the generic data type of the field to create for the test
   * @param pShouldLog   <tt>true</tt> if logging should happen
   */
  private void _checkLogging(Class<?> pGenericType, boolean pShouldLog)
  {
    clearInvocations(mockedLogger);
    new GenericField<>(pGenericType, "testName", Collections.emptyList());
    verify(mockedLogger, times(pShouldLog ? 1 : 0)).warning(anyString());
  }

  /**
   * Some bean type.
   */
  public static class SomeBean extends OJBean<SomeBean>
  {
  }

  /**
   * Some enum type.
   */
  public enum TestEnum
  {
  }
}
