package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.cdi.AbstractCdiTest;
import de.adito.ojcms.sql.datasource.persistence.SQLBeanDataStorage;
import de.adito.ojcms.sql.datasource.util.OJSQLException;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.*;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.util.*;

import static java.util.Collections.singleton;

/**
 * Base class for database tests. This test uses an in-memory database to improve performance.
 * Every actual test class has to provide a {@link IPersistenceModel} type for which a database table will be created.
 * This base class provides functionality to add some data to that database table via the persistence model.
 * The changes made to the database during one test method will be rolled back after each test.
 *
 * @author Simon Danner, 04.01.2020
 */
public abstract class AbstractDatabaseTest<MODEL extends IPersistenceModel> extends AbstractCdiTest
{
  protected static final String CONTAINER_ID = "containerId";

  @Inject
  protected OJSQLBuilder builder;
  @Inject
  protected SQLBeanDataStorage storage;

  protected MODEL model;

  /**
   * The persistence model type to create a database table for.
   *
   * @return the type of the persistence model
   */
  protected abstract Class<MODEL> getModelType();

  @BeforeEach
  public void setupDatabase()
  {
    model = _initModel();
    MockedPersistenceModels.model = model;
    model.initModelInDatabase(builder);
  }

  @AfterEach
  public void rollback()
  {
    storage.rollbackChanges();
  }

  /**
   * Adds a {@link SomeBean} to a persistent bean container.
   * Only possible if the model type is assignable from {@link ContainerPersistenceModel}.
   *
   * @param pIndex       the index to add the bean at
   * @param pFirstValue  the first value of the new bean
   * @param pSecondValue the second value of the new bean
   * @param pThirdValue  the third value of the new bean
   * @return the persistent bean data of the added bean
   */
  protected PersistentBeanData addContentToContainer(int pIndex, int pFirstValue, String pSecondValue, boolean pThirdValue)
  {
    final SomeBean bean = new SomeBean(pFirstValue, pSecondValue, pThirdValue);
    final Set<BeanAddition> additions = singleton(new BeanAddition(pIndex, bean.toMap(), SomeBean.class, CONTAINER_ID));
    storage.processAdditionsForContainer(CONTAINER_ID, additions);
    return new PersistentBeanData(pIndex, bean.toMap());
  }

  /**
   * Adds a sub type of {@link SomeBean} to the persistent base bean container.
   * Only possible if the model type is assignable from {@link BaseContainerPersistenceModel}.
   *
   * @param pIndex    the index to add the bean at
   * @param pBaseBean the sub type bean instance to add
   * @return the persistent bean data of the added bean
   */
  protected PersistentBeanData addContentToBaseContainer(int pIndex, SomeBean pBaseBean)
  {
    final Set<BeanAddition> additions = singleton(new BeanAddition(pIndex, pBaseBean.toMap(), pBaseBean.getClass(), CONTAINER_ID));
    storage.processAdditionsForContainer(CONTAINER_ID, additions);
    return new PersistentBeanData(pIndex, pBaseBean.toMap());
  }

  /**
   * Sets data for {@link SomeBean} that is registered as a persistent single bean.
   * Only possible if the model type is assignable from {@link SingleBeanPersistenceModel}.
   *
   * @param pFirstValue  the new first value of the single bean
   * @param pSecondValue the new second value of the single bean
   * @param pThirdValue  the new  third value of the single bean
   */
  protected void setSingleBeanValues(int pFirstValue, String pSecondValue, boolean pThirdValue)
  {
    final SomeBean bean = new SomeBean(pFirstValue, pSecondValue, pThirdValue);
    storage.processChangesForSingleBean(new SingleBeanKey(CONTAINER_ID), bean.toMap());
  }

  /**
   * Creates field value tuple based identifiers for an instance of {@link SomeBean}.
   *
   * @param pFirstValue  the first value to identify
   * @param pSecondValue the second value to identify
   * @return the created identifiers as map
   */
  protected Map<IField<?>, Object> createIdentifiers(int pFirstValue, String pSecondValue)
  {
    final Map<IField<?>, Object> identifiers = new HashMap<>();
    identifiers.put(SomeBean.FIELD1, pFirstValue);
    identifiers.put(SomeBean.FIELD2, pSecondValue);
    return identifiers;
  }

  /**
   * Initializes the {@link IPersistenceModel} for this test.
   * Also initializes the single bean table for associated models.
   *
   * @return the created persistence model instance
   */
  private MODEL _initModel()
  {
    final Class<MODEL> modelType = getModelType();

    if (modelType == ContainerPersistenceModel.class)
      //noinspection unchecked
      return (MODEL) new ContainerPersistenceModel(CONTAINER_ID, SomeBean.class);
    else if (modelType == BaseContainerPersistenceModel.class)
    {
      final Set<Class<? extends IBean>> subTypes = new HashSet<>(Arrays.asList(SomeOtherBean.class, SomeSpecialBean.class));
      //noinspection unchecked
      return (MODEL) new BaseContainerPersistenceModel(CONTAINER_ID, subTypes);
    }
    else if (modelType == SingleBeanPersistenceModel.class)
    {
      SingleBeanPersistenceModel.createSingleBeanTableIfNecessary(builder);
      //noinspection unchecked
      return (MODEL) new SingleBeanPersistenceModel(CONTAINER_ID, SomeBean.class);
    }
    else
      throw new OJSQLException("Unsupported model type: " + modelType.getName());
  }

  /**
   * Some bean to use for the persistence models and the tests.
   */
  public static class SomeBean extends OJBean
  {
    @Identifier
    public static final IntegerField FIELD1 = OJFields.create(SomeBean.class);
    @Identifier
    public static final TextField FIELD2 = OJFields.create(SomeBean.class);
    public static final BooleanField FIELD3 = OJFields.create(SomeBean.class);

    private SomeBean(int pValue1, String pValue2, boolean pValue3)
    {
      setValue(FIELD1, pValue1);
      setValue(FIELD2, pValue2);
      setValue(FIELD3, pValue3);
    }
  }

  /**
   * Some other bean extending {@link SomeBean}.
   */
  public static class SomeOtherBean extends SomeBean
  {
    public static final IntegerField FIELD4 = OJFields.create(SomeOtherBean.class);

    SomeOtherBean(int pValue1, String pValue2, boolean pValue3, int pValue4)
    {
      super(pValue1, pValue2, pValue3);
      setValue(FIELD4, pValue4);
    }
  }

  /**
   * Some very special bean extending {@link SomeOtherBean}.
   */
  public static class SomeSpecialBean extends SomeOtherBean
  {
    public static final TextField FIELD5 = OJFields.create(SomeSpecialBean.class);

    SomeSpecialBean(int pValue1, String pValue2, boolean pValue3, int pValue4, String pValue5)
    {
      super(pValue1, pValue2, pValue3, pValue4);
      setValue(FIELD5, pValue5);
    }
  }

  /**
   * Mocked {@link PersistenceModels} that always returns the model for this test.
   */
  @ApplicationScoped
  @Alternative
  @Priority(100)
  private static class MockedPersistenceModels extends PersistenceModels
  {
    private static IPersistenceModel model;

    @Override
    public ContainerPersistenceModel getContainerPersistenceModel(String pContainerId)
    {
      if (CONTAINER_ID.equals(pContainerId))
        return (ContainerPersistenceModel) model;

      throw new IllegalArgumentException("No model for container id " + pContainerId);
    }

    @Override
    public SingleBeanPersistenceModel getSingleBeanPersistenceModel(String pBeanId)
    {
      if (CONTAINER_ID.equals(pBeanId))
        return (SingleBeanPersistenceModel) model;

      throw new IllegalArgumentException("No model for bean id " + pBeanId);
    }
  }
}
