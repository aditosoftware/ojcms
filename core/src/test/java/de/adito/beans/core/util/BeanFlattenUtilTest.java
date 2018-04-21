package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.*;
import de.adito.beans.core.util.beancopy.BeanFlattenUtil;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BeanFlattenUtil}.
 *
 * @author Simon Danner, 09.04.2018
 */
class BeanFlattenUtilTest
{
  @Test
  public void testFlattingNotDeepNoCopy()
  {
    List<FieldTuple<?>> tuples = _flat(false);
    assertEquals(tuples.size(), 4);
    _checkFieldAmount(tuples, Person.name, 2);
    _checkFieldAmount(tuples, Person.address, 2);
  }

  @Test
  public void testFlattingDeepNoCopy()
  {
    List<FieldTuple<?>> tuples = _flat(true);
    assertEquals(tuples.size(), 6);
    _checkFieldAmount(tuples, Person.name, 2);
    _checkFieldAmount(tuples, Address.city, 2);
    _checkFieldAmount(tuples, Address.postalCode, 2);
  }

  @Test
  public void testFlattingDeepWithCopy()
  {
    Data original = new Data();
    Data flatBean = BeanFlattenUtil.makeFlat(original, true, true);
    //Remove mappers and change a value at the original bean
    original.clearDataMappers();
    final String newValue = "someName";
    original.getValue(Data.person1).setValue(Person.name, newValue);
    //Check if the copy is not affected from changes at the original
    List<FieldTuple<?>> tuples = flatBean.stream().collect(Collectors.toList());
    assertEquals(tuples.size(), 6);
    tuples.stream()
        .filter(pFieldTuple -> pFieldTuple.getField().getType() == String.class)
        .map(pFieldTuple -> (String) pFieldTuple.getValue())
        .forEach(pValue -> assertNotEquals(pValue, newValue)); //The value change in the original bean should not affect the copy
  }

  @Test
  public void testNormalize()
  {
    Data original = new Data();
    Data flatBean = BeanFlattenUtil.makeFlat(original, false, false);
    _checkFieldAmount(flatBean.stream().collect(Collectors.toList()), null, 4);
    BeanFlattenUtil.normalize(flatBean);
    _checkFieldAmount(flatBean.stream().collect(Collectors.toList()), null, 2);
  }

  /**
   * Makes an assertion, if a expected number of tuples with a certain bean field is contained in a collection of tuples.
   *
   * @param pTuples         the tuples to check
   * @param pFieldToCheck   the field to count the tuples (null, if all tuples should be included)
   * @param pExpectedAmount the expected amount of tuples
   */
  private void _checkFieldAmount(Collection<FieldTuple<?>> pTuples, @Nullable IField<?> pFieldToCheck, int pExpectedAmount)
  {
    assertEquals(pTuples.stream()
                     .map(FieldTuple::getField)
                     .filter(pField -> pFieldToCheck == null || pField == pFieldToCheck)
                     .count(), pExpectedAmount);
  }

  /**
   * Creates a flat test bean and returns the field tuples of that bean
   *
   * @param pDeep <tt>true</tt> if fields should flattened deeply (includes deep fields iteratively)
   * @return a list of field tuples of the flat bean
   */
  private List<FieldTuple<?>> _flat(boolean pDeep)
  {
    Data flatBean = BeanFlattenUtil.makeFlat(new Data(), pDeep, false);
    return flatBean.stream().collect(Collectors.toList());
  }

  /**
   * Some data POJO that manages a person registry.
   */
  public static class Data extends Bean<Data>
  {
    public static final BeanField<Person> person1 = BeanFieldFactory.create(Data.class);
    public static final BeanField<Person> person2 = BeanFieldFactory.create(Data.class);

    public Data()
    {
      setValue(person1, new Person());
      setValue(person2, new Person());
    }
  }

  /**
   * Bean for a person with an address property (reference).
   */
  public static class Person extends Bean<Person>
  {
    public static final TextField name = BeanFieldFactory.create(Person.class);
    public static final BeanField<Address> address = BeanFieldFactory.create(Person.class);

    public Person()
    {
      setValue(name, UUID.randomUUID().toString());
      setValue(address, new Address());
    }
  }

  /**
   * Bean for an address.
   */
  public static class Address extends Bean<Address>
  {
    public static final TextField city = BeanFieldFactory.create(Address.class);
    public static final IntegerField postalCode = BeanFieldFactory.create(Address.class);

    public Address()
    {
      setValue(city, UUID.randomUUID().toString());
      setValue(postalCode, 11111);
    }
  }
}