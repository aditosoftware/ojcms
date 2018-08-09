package de.adito.beans.core.util.beancopy;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean copying mechanism.
 *
 * @author Simon Danner, 19.04.2018
 */
class BeanCopyTest
{
  @Test
  public void testShallowCopy()
  {
    Data original = new Data();
    Data copy = original.createCopy(ECopyMode.SHALLOW_ONLY_BEAN_FIELDS);
    assertNotSame(original, copy);
    final String newName = "newName";
    original.getValue(Data.person1).setValue(Person.name, newName);
    assertEquals(copy.getValue(Data.person1).getValue(Person.name), newName);
  }

  @Test
  public void testShallowCopyWithCustomConstructor()
  {
    Data original = new Data();
    Data copy = original.createCopy(ECopyMode.SHALLOW_ONLY_BEAN_FIELDS, pData -> new Data(new Person(), new Person()));
    assertNotSame(original, copy);
    assertSame(original.getValue(Data.person1), copy.getValue(Data.person1));
  }

  @Test
  public void testDeepCopy()
  {
    Data original = new Data();
    Data copy = original.createCopy(ECopyMode.DEEP_ONLY_BEAN_FIELDS);
    original.getValue(Data.person1).getValue(Person.address).setValue(Address.city, null);
    assertNotNull(copy.getValue(Data.person1).getValue(Person.address).getValue(Address.city));
  }

  @Test
  public void testDeepCopyWithCustomCopy()
  {
    final String copyValue = "copy";
    Data original = new Data();
    Data copy = original.createCopy(ECopyMode.DEEP_ONLY_BEAN_FIELDS, Address.city.customFieldCopy(pValue -> copyValue));
    assertEquals(copy.getValue(Data.person1).getValue(Person.address).getValue(Address.city), copyValue);
  }

  @Test
  public void testAllFieldCopy()
  {
    Data original = new Data();
    Data copy = original.createCopy(ECopyMode.DEEP_ALL_FIELDS);
    assertEquals(original.someNormalList, copy.someNormalList);
    assertEquals(original.getValue(Data.person1).getValue(Person.address).someNormalField,
                 copy.getValue(Data.person1).getValue(Person.address).someNormalField);
  }

  /**
   * Some data POJO that manages a person registry.
   */
  public static class Data extends Bean<Data>
  {
    public static final BeanField<Person> person1 = BeanFieldFactory.create(Data.class);
    public static final BeanField<Person> person2 = BeanFieldFactory.create(Data.class);
    private List<Integer> someNormalList = new ArrayList<>();

    public Data()
    {
      this(new Person(), new Person());
    }

    public Data(Person pPerson1, Person pPerson2)
    {
      setValue(person1, pPerson1);
      setValue(person2, pPerson2);
      someNormalList.add(42);
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
    private final String someNormalField = "value";

    public Address()
    {
      setValue(city, UUID.randomUUID().toString());
      setValue(postalCode, 11111);
    }
  }
}