package de.adito.beans.core.references;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link IHierarchicalBeanStructure}.
 * It covers all types of references (deep or direct).
 *
 * @author Simon Danner, 15.03.2018
 */
class HierarchicalStructureTest
{
  @Test
  public void testDirectParentsBean()
  {
    Person person = new Person();
    Set<IHierarchicalStructure.IHierarchicalNode> parents = person.getValue(Person.address).getHierarchicalStructure().getDirectParents();
    assertEquals(1, parents.size());
    IHierarchicalStructure.IHierarchicalNode node = parents.iterator().next();
    assertSame(node.getBean(), person);
    assertSame(node.getField(), Person.address);
  }

  @Test
  public void testDirectParentsContainer()
  {
    PersonRegistry registry = new PersonRegistry();
    List<IHierarchicalStructure.IHierarchicalNode> nodes = registry.getValue(PersonRegistry.persons).stream()
        .flatMap(pPerson -> pPerson.getHierarchicalStructure().getDirectParents().stream())
        .collect(Collectors.toList());
    assertEquals(3, nodes.size());
    nodes.forEach(pNode -> {
      assertSame(pNode.getBean(), registry);
      assertSame(pNode.getField(), PersonRegistry.persons);
    });
  }

  @Test
  public void testAllParentReferencesByBean()
  {
    Data data = new Data();
    Address deepAddress = data.getValue(Data.registry).getValue(PersonRegistry.persons).getBean(0).getValue(Person.address);
    Set<IHierarchicalField<?>> referencesByBean = deepAddress.getHierarchicalStructure().getParentReferencesByBean(data);
    assertEquals(1, referencesByBean.size());
    assertTrue(referencesByBean.contains(Data.registry));
  }

  @Test
  public void testAllParentReferencesByField()
  {
    Data data = new Data();
    Address deepAddress = data.getValue(Data.registry).getValue(PersonRegistry.persons).getBean(0).getValue(Person.address);
    Set<IBean<?>> referencesByField = deepAddress.getHierarchicalStructure().getParentReferenceByField(Data.registry);
    assertEquals(1, referencesByField.size());
    assertTrue(referencesByField.contains(data));
  }

  /**
   * Some data POJO that manages a person registry.
   */
  public static class Data extends Bean<Data>
  {
    public static final BeanField<PersonRegistry> registry = BeanFieldFactory.create(Data.class);

    public Data()
    {
      setValue(registry, new PersonRegistry());
    }
  }

  /**
   * A bean that holds multiple persons through a bean container field.
   */
  public static class PersonRegistry extends Bean<PersonRegistry>
  {
    public static final ContainerField<Person> persons = BeanFieldFactory.create(PersonRegistry.class);

    public PersonRegistry()
    {
      setValue(persons, IBeanContainer.ofVariableNotEmpty(new Person(), new Person(), new Person()));
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