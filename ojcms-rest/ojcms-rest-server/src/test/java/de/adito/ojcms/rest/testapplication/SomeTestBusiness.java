package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.beans.IBeanContainer;
import de.adito.ojcms.rest.security.UserRequestContext;
import de.adito.ojcms.rest.security.user.OJUser;
import de.adito.ojcms.transactions.annotations.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

import static de.adito.ojcms.rest.security.user.OJUser.DISPLAY_NAME;

/**
 * Test business to test the additionally added persistent bean type.
 *
 * @author Simon Danner, 09.05.2020
 */
@ApplicationScoped
class SomeTestBusiness
{
  private static final Logger LOGGER = Logger.getLogger(SomeTestBusiness.class.getName());

  @Inject
  private IBeanContainer<SomeAdditionalTestBean> additionalBeans;
  @Inject
  private UserRequestContext userRequestContext;

  @Transactional
  void doIt()
  {
    final OJUser requestingUser = userRequestContext.getRequestingUser();
    LOGGER.info("Requesting user for business: " + requestingUser.getValue(DISPLAY_NAME));
    additionalBeans.addBean(new SomeAdditionalTestBean());
  }
}
