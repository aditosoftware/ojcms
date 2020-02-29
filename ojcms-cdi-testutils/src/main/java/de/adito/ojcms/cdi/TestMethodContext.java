package de.adito.ojcms.cdi;

import de.adito.ojcms.cdi.context.*;

import java.lang.annotation.Annotation;

/**
 * Custom CDI context for test method scope.
 *
 * @author Simon Danner, 29.02.2020
 */
@CustomCdiContext
public class TestMethodContext extends AbstractCustomContext
{
  @Override
  public Class<? extends Annotation> getScope()
  {
    return TestMethodScoped.class;
  }
}
