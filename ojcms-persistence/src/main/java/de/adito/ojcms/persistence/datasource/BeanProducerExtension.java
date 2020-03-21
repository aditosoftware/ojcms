package de.adito.ojcms.persistence.datasource;


import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.datasource.IBeanContainerDataSource;
import de.adito.ojcms.persistence.*;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.utils.StringUtility;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * CDI extension discovering persistent bean types and creating producer beans for the required bean containers and single beans.
 * Also stores the persistent beans types to be statically accessible.
 *
 * @author Simon Danner, 30.12.2019
 */
class BeanProducerExtension implements Extension
{
  static final Map<Class<? extends IBean>, String> CONTAINER_BEAN_TYPES = new HashMap<>();
  static final Map<Class<? extends IBean>, BaseContainerRegistration> BASE_CONTAINER_TYPES = new HashMap<>();
  static final Map<Class<? extends IBean>, String> SINGLE_BEAN_TYPES = new HashMap<>();

  /**
   * Scans every {@link AnnotatedType} for persistent bean types.
   * If the bean type has a {@link Persist} or {@link PersistAsBaseType} annotation, the types will be registered to create producers
   * later on.
   *
   * @param pProcessedBean a processed bean type
   */
  void findPersistentBeans(@Observes ProcessAnnotatedType<? extends IBean> pProcessedBean)
  {
    final AnnotatedType<? extends IBean> type = pProcessedBean.getAnnotatedType();
    //noinspection unchecked
    final Class<? extends IBean> beanType = (Class<? extends IBean>) type.getBaseType();

    if (type.isAnnotationPresent(Persist.class))
      _registerPersistentBeanType(type.getAnnotation(Persist.class), beanType, pProcessedBean);
    else if (type.isAnnotationPresent(PersistAsBaseType.class))
      _registerPersistentBaseContainer(type.getAnnotation(PersistAsBaseType.class), beanType);
  }

  /**
   * Creates producers for persistent bean containers and single beans after CDI bean discovery.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   */
  void afterBeanDiscovery(@Observes AfterBeanDiscovery pAfterBeanDiscovery)
  {
    CONTAINER_BEAN_TYPES.forEach((pBeanType, pContainerId) -> _addContainerCdiBeans(pAfterBeanDiscovery, pBeanType, pContainerId));
    BASE_CONTAINER_TYPES.forEach((pBaseType, pRegistration) -> _addBaseContainerCdiBeans(pAfterBeanDiscovery, pBaseType, pRegistration));
    SINGLE_BEAN_TYPES.forEach((pBeanType, pBeanId) -> _addSingleBeanCdiBeans(pAfterBeanDiscovery, pBeanType, pBeanId));
  }

  /**
   * Registers a persistent bean annotated by {@link Persist}.
   * If the bean should be persisted in {@link EPersistenceMode#SINGLE} the processed bean will be vetoed.
   * The producer should be used instead.
   *
   * @param pAnnotation    the persistence annotation instance to retrieve configuration from
   * @param pBeanType      the annotated bean type
   * @param pProcessedBean the processed CDI bean
   */
  private static void _registerPersistentBeanType(Persist pAnnotation, Class<? extends IBean> pBeanType,
                                                  ProcessAnnotatedType<? extends IBean> pProcessedBean)
  {
    if (Modifier.isAbstract(pBeanType.getModifiers()))
      throw new OJPersistenceException("Abstract bean type " + pBeanType.getName() + " cannot be persisted!");

    if (pAnnotation.mode() == EPersistenceMode.CONTAINER)
      CONTAINER_BEAN_TYPES.put(pBeanType, pAnnotation.containerId());
    else
    {
      SINGLE_BEAN_TYPES.put(pBeanType, pAnnotation.containerId());
      pProcessedBean.veto();
    }
  }

  /**
   * Registers a persistent base bean type annotated by {@link PersistAsBaseType}.
   * A base container will be registered for all {@link PersistAsBaseType#forSubTypes()}.
   * Checks several conditions before registering the bean type.
   *
   * @param pAnnotation the persistence annotation instance to retrieve configuration from
   * @param pBeanType   the annotated base bean type
   */
  private static void _registerPersistentBaseContainer(PersistAsBaseType pAnnotation, Class<? extends IBean> pBeanType)
  {
    final Set<Class<? extends IBean>> subTypes = new HashSet<>();

    if (pAnnotation.forSubTypes().length < 2)
      throw new OJPersistenceException("At least two sub types must be given to create a persistent container for " + pBeanType.getName());

    for (Class<? extends IBean> subType : pAnnotation.forSubTypes())
    {
      if (Modifier.isAbstract(subType.getModifiers()))
        throw new OJPersistenceException(
            "Sub bean type " + subType.getName() + " is abstract! " + "Cannot be used for base container " + pBeanType.getName());

      if (!pBeanType.isAssignableFrom(subType))
        throw new OJPersistenceException("Sub bean type " + subType.getName() + " is not assignable from " + pBeanType
            .getName() + "!" + "Cannot be used for base container!");

      subTypes.add(subType);
    }

    final BaseContainerRegistration registration = new BaseContainerRegistration(pAnnotation.containerId(), subTypes);
    BASE_CONTAINER_TYPES.put(pBeanType, registration);
  }

  /**
   * Creates producer beans for a persistent bean container.
   * This includes the {@link IBeanContainer} itself and a {@link TransactionalScoped} {@link ContainerContent}
   * managing the content per transaction.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   * @param pBeanType           the bean type of the persistent container
   * @param pContainerId        the id of the persistent bean container
   */
  private static void _addContainerCdiBeans(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean> pBeanType, String pContainerId)
  {
    final ContainerQualifier literal = ContainerQualifier.Literal.forContainerId(pContainerId);
    _registerContainerCdiBean(pAfterBeanDiscovery, pBeanType, literal);
    _registerContainerContentCdiBean(pAfterBeanDiscovery, pContainerId, pBeanType, literal);
  }

  /**
   * Creates producer beans for a persistent base bean container.
   * This includes the base {@link IBeanContainer} itself and a {@link TransactionalScoped} {@link ContainerContent}
   * managing the content per transaction.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   * @param pBeanBaseType       the base type bean of the persistent container
   * @param pRegistration       information about the registered base bean container
   */
  private static void _addBaseContainerCdiBeans(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean> pBeanBaseType,
                                                BaseContainerRegistration pRegistration)
  {
    final ContainerQualifier literal = ContainerQualifier.Literal.forContainerId(pRegistration.getContainerId());
    _registerContainerCdiBean(pAfterBeanDiscovery, pBeanBaseType, literal);
    _registerContainerContentCdiBean(pAfterBeanDiscovery, pRegistration.getContainerId(), null, literal);
  }

  /**
   * Creates producer beans for a persistent single bean.
   * This includes the {@link IBean} instance itself and a {@link TransactionalScoped} {@link SingleBeanContent}
   * managing the content of the bean per transaction.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   * @param pBeanType           the bean type of the persistent single bean
   * @param pBeanId             the id of the persistent single bean
   */
  private static void _addSingleBeanCdiBeans(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean> pBeanType, String pBeanId)
  {
    final ContainerQualifier literal = ContainerQualifier.Literal.forContainerId(pBeanId);

    //noinspection unchecked
    pAfterBeanDiscovery.addBean() //
        .scope(Dependent.class) //
        .types(pBeanType) //
        .produceWith(pEnvironment -> _createSingleBean(pEnvironment, (Class) pBeanType, literal));

    pAfterBeanDiscovery.addBean() //
        .scope(TransactionalScoped.class) //
        .qualifiers(literal) //
        .types(SingleBeanContent.class) //
        .produceWith(pEnvironment -> _createSingleBeanContent(pEnvironment, pBeanId, pBeanType));
  }

  /**
   * Registers the CDI producer for a persistent bean container.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   * @param pBeanType           the type of the beans in the persistent container
   * @param pLiteral            a CDI literal to identify the container content
   */
  private static void _registerContainerCdiBean(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean> pBeanType,
                                                ContainerQualifier pLiteral)
  {
    //noinspection unchecked
    pAfterBeanDiscovery.addBean() //
        .scope(Dependent.class) //
        .types(IBeanContainer.class, new ParameterizedTypeImpl(IBeanContainer.class, pBeanType)) //
        .produceWith(pEnvironment -> _createBeanContainer(pEnvironment, (Class) pBeanType, pLiteral));
  }

  /**
   * Registers the CDI producer for a {@link ContainerContent}.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   * @param pContainerId        the id of the container
   * @param pBeanType           the type of the beans in the persistent container
   * @param pLiteral            a CDI literal to identify the container content
   */
  private static void _registerContainerContentCdiBean(AfterBeanDiscovery pAfterBeanDiscovery, String pContainerId, //
                                                       @Nullable Class<? extends IBean> pBeanType, ContainerQualifier pLiteral)
  {
    //noinspection unchecked
    pAfterBeanDiscovery.addBean() //
        .scope(TransactionalScoped.class) //
        .qualifiers(pLiteral) //
        .types(ContainerContent.class) //
        .produceWith(pEnvironment -> _createContainerContent(pEnvironment, pContainerId, (Class) pBeanType));
  }

  /**
   * Creates a {@link IBeanContainer} instance for a persistent bean container.
   *
   * @param pEnvironment a CDI instance to create managed CDI beans
   * @param pBeanType    the bean type of the bean container
   * @param pLiteral     a qualifier annotation literal to identify the persistent container
   * @return the created bean container instance
   */
  private static <BEAN extends IBean> IBeanContainer<BEAN> _createBeanContainer(Instance<Object> pEnvironment, Class<BEAN> pBeanType,
                                                                                ContainerQualifier pLiteral)
  {
    //noinspection unchecked
    final ContainerContent<BEAN> content = pEnvironment.select(ContainerContent.class, pLiteral).get();
    final IBeanContainerDataSource<BEAN> dataSource = new PersistentContainerDatasource<>(content);
    return IBeanContainer.withCustomDataSource(pBeanType, dataSource);
  }

  /**
   * Creates a {@link ContainerContent} for a persistent {@link IBeanContainer}.
   *
   * @param pEnvironment a CDI instance to create managed CDI beans
   * @param pContainerId the persistence container id of the persistent container
   * @param pBeanType    the bean type of the bean container
   * @return the created instance managing the persistent container's content
   */
  private static <BEAN extends IBean> ContainerContent<BEAN> _createContainerContent(Instance<Object> pEnvironment, String pContainerId,
                                                                                     Class<BEAN> pBeanType)
  {
    return new ContainerContent<>(pContainerId, pBeanType, pEnvironment.select(ITransaction.class).get());
  }

  /**
   * Creates a {@link IBean} instance for a persistent single bean.
   *
   * @param pEnvironment a CDI instance to create managed CDI beans
   * @param pBeanType    the type of the single bean
   * @param pLiteral     a qualifier annotation literal to identify the single persistent bean
   * @return the created single bean instance
   */
  private static <BEAN extends IBean> BEAN _createSingleBean(Instance<Object> pEnvironment, Class<BEAN> pBeanType,
                                                             ContainerQualifier pLiteral)
  {
    final SingleBeanContent content = pEnvironment.select(SingleBeanContent.class, pLiteral).get();
    final PersistentBeanDatasource datasource = new PersistentBeanDatasource(content);
    return BeanPersistenceUtil.newPersistentBeanInstance(pBeanType, datasource);
  }

  /**
   * Creates a {@link SingleBeanContent} for a persistent single bean to manage its content.
   *
   * @param pEnvironment a CDI instance to create managed CDI beans
   * @param pBeanId      the persistence id of the single bean
   * @param pBeanType    the type of the single bean to create the content for
   * @return the created instance managing the bean's content
   */
  private static SingleBeanContent<?> _createSingleBeanContent(Instance<Object> pEnvironment, String pBeanId,
                                                               Class<? extends IBean> pBeanType)
  {
    final ITransaction transaction = pEnvironment.select(ITransaction.class).get();
    return new SingleBeanContent<>(new SingleBeanKey(pBeanId), pBeanType, transaction);
  }

  /**
   * Defines a base container registration from {@link PersistAsBaseType}.
   * Holds the container id and a set of sub types the base container should be created for.
   */
  static class BaseContainerRegistration
  {
    private final String containerId;
    private final Set<Class<? extends IBean>> subTypes;

    /**
     * Creates a new base container registration.
     *
     * @param pContainerId the id of the persistent base container
     * @param pSubTypes    all sub types the base container is for
     */
    BaseContainerRegistration(String pContainerId, Set<Class<? extends IBean>> pSubTypes)
    {
      containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
      subTypes = new HashSet<>(pSubTypes);
    }

    /**
     * The id of the persistent base container
     */
    String getContainerId()
    {
      return containerId;
    }

    /**
     * All sub types the base container is for
     */
    Set<Class<? extends IBean>> getSubTypes()
    {
      return subTypes;
    }
  }
}
