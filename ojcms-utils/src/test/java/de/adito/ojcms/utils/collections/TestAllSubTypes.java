package de.adito.ojcms.utils.collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.*;

/**
 * Used for {@link AbstractInterfaceTest} to mark test methods that should be executed for every sub type of the interface to test.
 *
 * @author Simon Danner, 11.01.2020
 */
@Documented
@ParameterizedTest
@MethodSource("de.adito.ojcms.utils.collections.AbstractInterfaceTest#getInstanceArguments")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestAllSubTypes
{
}
