package de.adito.beans.core.util.weak;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.*;
import java.util.stream.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Interface based tests for {@link IInputSortedElements}.
 *
 * @author Simon Danner, 07.03.2018
 */
class InputSortedElementsTest
{
  /**
   * Defines the argument instances to test.
   * The tests will be executed for each argument.
   *
   * @return a stream of arguments
   */
  private static Stream<Arguments> _toTest()
  {
    return Stream.of(
        Arguments.of(new WeakInputSortedContainer<_Person>())
        //Add other implementation classes here
    );
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testAddNullFail(IInputSortedElements<_Person> pElementContainer)
  {
    assertThrows(IllegalArgumentException.class, () -> pElementContainer.add(null));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testAddDuplicateFail(IInputSortedElements<_Person> pElementContainer)
  {
    _Person somePerson = new _Person("Some", "Name");
    pElementContainer.add(somePerson);
    assertFalse(pElementContainer.add(somePerson));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testAddOneElement(IInputSortedElements<_Person> pElementContainer)
  {
    _Person addedPerson = _addOne(pElementContainer);
    assertFalse(pElementContainer.isEmpty());
    assertEquals(pElementContainer.iterator().next(), addedPerson);
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testAddMultipleElementsOrdered(IInputSortedElements<_Person> pElementContainer)
  {
    final int amount = 10;
    List<_Person> addedPersons = _addMultiple(pElementContainer, amount);
    assertEquals(amount, pElementContainer.size());
    assertThat(addedPersons, Matchers.contains(pElementContainer.stream().toArray(_Person[]::new)));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testRemoveNull(IInputSortedElements<_Person> pElementContainer)
  {
    assertThrows(IllegalArgumentException.class, () -> pElementContainer.add(null));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testRemoveElement(IInputSortedElements<_Person> pElementContainer)
  {
    _Person addedPerson = _addOne(pElementContainer);
    assertTrue(pElementContainer.remove(addedPerson));
    assertTrue(pElementContainer.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testClear(IInputSortedElements<_Person> pElementContainer)
  {
    _addMultiple(pElementContainer, 10);
    pElementContainer.clear();
    assertTrue(pElementContainer.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testSize(IInputSortedElements<_Person> pElementContainer)
  {
    assertEquals(0, pElementContainer.size());
    _addOne(pElementContainer);
    assertEquals(1, pElementContainer.size());
    _addMultiple(pElementContainer, 9);
    assertEquals(10, pElementContainer.size());
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testIsEmpty(IInputSortedElements<_Person> pElementContainer)
  {
    assertTrue(pElementContainer.isEmpty());
    _addOne(pElementContainer);
    assertFalse(pElementContainer.isEmpty());
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testContainsNull(IInputSortedElements<_Person> pElementContainer)
  {
    assertThrows(IllegalArgumentException.class, () -> pElementContainer.contains(null));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testContainsAfterAddition(IInputSortedElements<_Person> pElementContainer)
  {
    _Person testPerson = new _Person("Some", "Name");
    assertFalse(pElementContainer.contains(testPerson));
    pElementContainer.add(testPerson);
    assertTrue(pElementContainer.contains(testPerson));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void testContainsEqualsBased(IInputSortedElements<_Person> pElementContainer)
  {
    _Person testPerson1 = new _Person("Some", "Name");
    _Person testPerson2 = new _Person("Some", "Name");
    pElementContainer.add(testPerson1);
    assertTrue(pElementContainer.contains(testPerson2));
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  void stream(IInputSortedElements<_Person> pElementContainer)
  {
    Iterator<_Person> addedIterator = _addMultiple(pElementContainer, 10).iterator();
    //Check, if all elements are obtainable via the stream and the order of the elements in the stream
    pElementContainer.forEach(pElement -> assertEquals(pElement, addedIterator.next()));
  }

  /**
   * Adds one test instance to the element container.
   *
   * @param pElementContainer the element container
   * @return the added test instance
   */
  private _Person _addOne(IInputSortedElements<_Person> pElementContainer)
  {
    return _addMultiple(pElementContainer, 1).get(0);
  }

  /**
   * Adds multiple test instances to the element container.
   *
   * @param pElementContainer the element container
   * @param pAmount           the amount of instances to add
   * @return a list of added instances
   */
  private List<_Person> _addMultiple(IInputSortedElements<_Person> pElementContainer, int pAmount)
  {
    return IntStream.range(0, pAmount)
        .mapToObj(pIndex -> new _Person(UUID.randomUUID().toString(), "Name"))
        .peek(pElementContainer::add)
        .collect(Collectors.toList());
  }

  /**
   * Some POJO for testing.
   */
  private class _Person
  {
    private final String firstName, lastName;

    public _Person(String pFirstName, String pLastName)
    {
      firstName = pFirstName;
      lastName = pLastName;
    }

    @Override
    public boolean equals(Object pO)
    {
      if (this == pO)
        return true;
      if (pO == null || getClass() != pO.getClass())
        return false;
      _Person person = (_Person) pO;
      return Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(firstName, lastName);
    }
  }
}