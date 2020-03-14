package de.adito.ojcms.persistence.datasource;


import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.datasource.IBeanContainerDataSource;
import de.adito.ojcms.persistence.*;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.*;
import java.util.*;

/**
 * CDI extension discovering persistent bean types and creating producer beans for the required bean containers and single beans.
 * Also stores the persistent beans types to be statically accessible.
 *
 * @author Simon Danner, 30.12.2019
 */
class BeanProducerExtension implements Extension
{
  static final Map<Class<? extends IBean<?>>, String> CONTAINER_BEAN_TYPES = new HashMap<>();
  static final Map<Class<? extends IBean<?>>, String> SINGLE_BEAN_TYPES = new HashMap<>();

  /**
   * Scans every {@link AnnotatedType} for persistent bean types.
   * If the bean type has a {@link Persist} annotation, the type will be registered to create producers later on.
   *
   * @param pProcessedBean a processed bean type
   */
  void findPersistentBeans(@Observes ProcessAnnotatedType<? extends IBean<?>> pProcessedBean)
  {
    final Persist persistenceAnnotation = pProcessedBean.getAnnotatedType().getAnnotation(Persist.class);

    if (persistenceAnnotation == null)
      return;

    //noinspection unchecked
    final Class<? extends IBean<?>> beanType = (Class<? extends IBean<?>>) pProcessedBean.getAnnotatedType().getBaseType();

    if (persistenceAnnotation.mode() == EPersistenceMode.CONTAINER)
      CONTAINER_BEAN_TYPES.put(beanType, persistenceAnnotation.containerId());
    else
    {
      SINGLE_BEAN_TYPES.put(beanType, persistenceAnnotation.containerId());
      pProcessedBean.veto();
    }
  }

  /**
   * Creates producers for persistent bean containers and single beans after CDI bean discovery.
   *
   * @param pAfterBeanDiscovery allows the addition of custom CDI beans
   */
  void afterBeanDiscovery(@Observes AfterBeanDiscovery pAfterBeanDiscovery)
  {
    CONTAINER_BEAN_TYPES.forEach((pBeanType, pContainerId) -> _addContainerCdiBeans(pAfterBeanDiscovery, pBeanType, pContainerId));
    SINGLE_BEAN_TYPES.forEach((pBeanType, pBeanId) -> _addSingleBeanCdiBeans(pAfterBeanDiscovery, pBeanType, pBeanId));
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
  private static void _addContainerCdiBeans(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean<?>> pBeanType, String pContainerId)
  {
    final ContainerQualifier literal = ContainerQualifier.Literal.forContainerId(pContainerId);

    //noinspection unchecked
    pAfterBeanDiscovery.addBean()
        .scope(Dependent.class)
        .types(IBeanContainer.class, new ParameterizedTypeImpl(IBeanContainer.class, pBeanType))
        .produceWith(pEnvironment -> _createBeanContainer(pEnvironment, (Class) pBeanType, literal));

    //noinspection unchecked
    pAfterBeanDiscovery.addBean()
        .scope(TransactionalScoped.class)
        .qualifiers(literal)
        .types(ContainerContent.class)
        .produceWith(pEnvironment -> _createContainerContent(pEnvironment, pContainerId, (Class) pBeanType));
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
  private static void _addSingleBeanCdiBeans(AfterBeanDiscovery pAfterBeanDiscovery, Class<? extends IBean<?>> pBeanType, String pBeanId)
  {
    final ContainerQualifier literal = ContainerQualifier.Literal.forContainerId(pBeanId);

    //noinspection unchecked
    pAfterBeanDiscovery.addBean()
        .scope(Dependent.class)
        .types(pBeanType)
        .produceWith(pEnvironment -> _createSingleBean(pEnvironment, (Class) pBeanType, literal));

    pAfterBeanDiscovery.addBean()
        .scope(TransactionalScoped.class)
        .qualifiers(literal)
        .types(SingleBeanContent.class)
        .produceWith(pEnvironment -> _createSingleBeanContent(pEnvironment, pBeanId));
  }

  /**
   * Creates a {@link IBeanContainer} instance for a persistent bean container.
   *
   * @param pEnvironment a CDI instance to create managed CDI beans
   * @param pBeanType    the bean type of the bean container
   * @param pLiteral     a qualifier annotation literal to identify the persistent container
   * @return the created bean container instance
   */
  private static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> _createBeanContainer(Instance<Object> pEnvironment, Class<BEAN> pBeanType,
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
  private static <BEAN extends IBean<BEAN>> ContainerContent<BEAN> _createContainerContent(Instance<Object> pEnvironment,
                                                                                           String pContainerId, Class<BEAN> pBeanType)
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
  private static <BEAN extends IBean<BEAN>> BEAN _createSingleBean(Instance<Object> pEnvironment, Class<BEAN> pBeanType,
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
   * @return the created instance managing the bean's content
   */
  private static SingleBeanContent _createSingleBeanContent(Instance<Object> pEnvironment, String pBeanId)
  {
    final ITransaction transaction = pEnvironment.select(ITransaction.class).get();
    return new SingleBeanContent(new SingleBeanKey(pBeanId), transaction);
  }
}
