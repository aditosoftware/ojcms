package de.adito.ojcms.utils.collections;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * Base class for interface based tests.
 * This class allows to provide any amount of instances implementing the interface to test.
 * Test methods marked with {@link TestAllSubTypes} will be executed for every instance.
 *
 * @author Simon Danner, 11.01.2020
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractInterfaceTest<INTERFACE>
{
  /**
   * A stream of instance arguments.
   * For every instance test methods marked with {@link TestAllSubTypes} will be executed.
   *
   * @return a stream of arguments for parameterized tests
   */
  protected Stream<Arguments> getInstanceArguments()
  {
    return getActualInstances().map(Arguments::arguments);
  }

  /**
   * A stream providing all instances implementing the interface to test.
   *
   * @return a stream of actual instances
   */
  protected abstract Stream<INTERFACE> getActualInstances();
}
