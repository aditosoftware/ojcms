package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.OptionalField;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.beans.util.ECopyMode;
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
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.SHALLOW_ONLY_BEAN_FIELDS);
    assertNotSame(original, copy);
    final String newName = "newName";
    original.getValue(Data.person1).setValue(Person.name, newName);
    assertEquals(copy.getValue(Data.person1).getValue(Person.name), newName);
  }

  @Test
  public void testShallowCopyWithCustomConstructor()
  {
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.SHALLOW_ONLY_BEAN_FIELDS, pData -> new Data(new Person(), new Person()));
    assertNotSame(original, copy);
    assertSame(original.getValue(Data.person1), copy.getValue(Data.person1));
  }

  @Test
  public void testDeepCopy()
  {
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.DEEP_ONLY_BEAN_FIELDS);
    original.getValue(Data.person1).getValue(Person.address).setValue(Address.city, null);
    assertNotNull(copy.getValue(Data.person1).getValue(Person.address).getValue(Address.city));
  }

  @Test
  public void testDeepCopyWithCustomCopy()
  {
    final String copyValue = "copy";
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.DEEP_ONLY_BEAN_FIELDS, Address.city.customFieldCopy(pValue -> copyValue));
    assertEquals(copy.getValue(Data.person1).getValue(Person.address).getValue(Address.city), copyValue);
  }

  @Test
  public void testAllFieldShallowCopy()
  {
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.SHALLOW_ALL_FIELDS);
    assertEquals(original.someNormalList, copy.someNormalList);
    assertSame(original.getValue(Data.person1).getValue(Person.address).someNormalField,
               copy.getValue(Data.person1).getValue(Person.address).someNormalField);
  }

  @Test
  public void testAllFieldDeepCopy()
  {
    final Data original = new Data(new Person(), new Person());
    final Data copy = original.createCopy(ECopyMode.DEEP_ALL_FIELDS);
    assertEquals(original.someNormalList, copy.someNormalList);
    assertNotSame(original.getValue(Data.person1).getValue(Person.address).someNormalField,
                  copy.getValue(Data.person1).getValue(Person.address).someNormalField);
  }

  @Test
  public void testCopyWithOptionalField()
  {
    final House original = new House();
    original.setValue(House.hasBasement, true);
    original.setValue(House.isBasementUnderGround, true);
    original.setValue(House.someBoringInfo, "info");
    original.setValue(House.anotherBoringInfo, "info");

    final House copy = original.createCopy(ECopyMode.SHALLOW_ONLY_BEAN_FIELDS);

    assertEquals(3, copy.streamFields().count());
    assertTrue(copy.getValue(House.isBasementUnderGround));
  }

  /**
   * Some data POJO that manages a person registry.
   */
  public static class Data extends OJBean<Data>
  {
    public static final BeanField<Person> person1 = OJFields.create(Data.class);
    public static final BeanField<Person> person2 = OJFields.create(Data.class);
    private List<Integer> someNormalList = Collections.singletonList(42);

    public Data(Person pPerson1, Person pPerson2)
    {
      setValue(person1, pPerson1);
      setValue(person2, pPerson2);
    }
  }

  /**
   * Bean for a person with an address property (reference).
   */
  public static class Person extends OJBean<Person>
  {
    public static final TextField name = OJFields.create(Person.class);
    public static final BeanField<Address> address = OJFields.create(Person.class);

    public Person()
    {
      setValue(name, UUID.randomUUID().toString());
      setValue(address, new Address());
    }
  }

  /**
   * Bean for an address.
   */
  public static class Address extends OJBean<Address>
  {
    public static final TextField city = OJFields.create(Address.class);
    public static final IntegerField postalCode = OJFields.create(Address.class);
    private final List<String> someNormalField = new ArrayList<>();

    public Address()
    {
      setValue(city, UUID.randomUUID().toString());
      setValue(postalCode, 11111);
    }
  }

  public static class House extends OJBean<House>
  {
    public static final BooleanField hasBasement = OJFields.create(House.class);
    @OptionalField
    public static final BooleanField isBasementUnderGround = OJFields.createOptional(House.class,
                                                                                     ((pHouse, pValue) -> pHouse.getValue(hasBasement)));
    @OptionalField
    public static final TextField someBoringInfo = OJFields.createOptional(House.class, ((pHouse, pValue) -> pValue != null));
    @OptionalField
    public static final TextField anotherBoringInfo = OJFields.createOptional(House.class, ((pHouse, pValue) -> pValue == null));
  }
}