package de.adito.ojcms.beans.references;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.types.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean reference utility methods.
 * It covers all types of references (all and direct). See {@link IBean} for the associated methods.
 *
 * @author Simon Danner, 15.03.2018
 */
class BeanReferenceTest
{
  @Test
  public void testDirectParentsBean()
  {
    final Person person = new Person();
    final Set<BeanReference> directReferences = person.getValue(Person.address).getDirectReferences();
    assertEquals(1, directReferences.size());
    final BeanReference reference = directReferences.iterator().next();
    assertSame(reference.getBean(), person);
    assertSame(reference.getField(), Person.address);
  }

  @Test
  public void testDirectParentsContainer()
  {
    final PersonRegistry registry = new PersonRegistry();
    final List<BeanReference> directReferences = registry.getValue(PersonRegistry.persons).stream()
        .flatMap(pPerson -> pPerson.getDirectReferences().stream())
        .collect(Collectors.toList());
    assertEquals(3, directReferences.size());
    directReferences.forEach(pNode -> {
      assertSame(pNode.getBean(), registry);
      assertSame(pNode.getField(), PersonRegistry.persons);
    });
  }

  @Test
  public void testAllParentReferencesByBean()
  {
    final Data data = new Data();
    final Address deepAddress = data.getValue(Data.registry).getValue(PersonRegistry.persons).getBean(0).getValue(Person.address);
    final Set<IField<?>> referencesByBean = deepAddress.getAllReferencesByBean(data);
    assertEquals(1, referencesByBean.size());
    assertTrue(referencesByBean.contains(Data.registry));
  }

  @Test
  public void testAllParentReferencesByField()
  {
    final Data data = new Data();
    final Address deepAddress = data.getValue(Data.registry).getValue(PersonRegistry.persons).getBean(0).getValue(Person.address);
    final Set<IBean> referencesByField = deepAddress.getAllReferencesByField(Data.registry);
    assertEquals(1, referencesByField.size());
    assertTrue(referencesByField.contains(data));
  }

  /**
   * Some data POJO that manages a person registry.
   */
  public static class Data extends OJBean
  {
    public static final BeanField<PersonRegistry> registry = OJFields.create(Data.class);

    public Data()
    {
      setValue(registry, new PersonRegistry());
    }
  }

  /**
   * A bean that holds multiple persons through a bean container field.
   */
  public static class PersonRegistry extends OJBean
  {
    public static final ContainerField<Person> persons = OJFields.create(PersonRegistry.class);

    public PersonRegistry()
    {
      setValue(persons, IBeanContainer.ofVariableNotEmpty(new Person(), new Person(), new Person()));
    }
  }

  /**
   * Bean for a person with an address property (reference).
   */
  public static class Person extends OJBean
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
  public static class Address extends OJBean
  {
    public static final TextField city = OJFields.create(Address.class);
    public static final IntegerField postalCode = OJFields.create(Address.class);

    public Address()
    {
      setValue(city, UUID.randomUUID().toString());
      setValue(postalCode, 11111);
    }
  }
}