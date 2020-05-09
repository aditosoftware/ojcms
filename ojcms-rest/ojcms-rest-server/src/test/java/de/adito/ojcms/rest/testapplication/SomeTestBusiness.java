package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.beans.IBeanContainer;
import de.adito.ojcms.transactions.annotations.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Test business to test the additionally added persistent bean type.
 *
 * @author Simon Danner, 09.05.2020
 */
@ApplicationScoped
class SomeTestBusiness
{
  @Inject
  private IBeanContainer<SomeAdditionalTestBean> additionalBeans;

  @Transactional
  void doIt()
  {
    additionalBeans.addBean(new SomeAdditionalTestBean());
  }
}
