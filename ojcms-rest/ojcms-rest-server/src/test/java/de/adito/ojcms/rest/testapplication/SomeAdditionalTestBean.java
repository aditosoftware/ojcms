package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.literals.fields.types.TextField;

/**
 * Some business bean for test.
 *
 * @author Simon Danner, 09.05.2020
 */
public class SomeAdditionalTestBean extends OJBean
{
  public static final TextField SOME_FIELD = OJFields.create(SomeAdditionalTestBean.class);

  public SomeAdditionalTestBean()
  {
    setValue(SOME_FIELD, "someValue");
  }
}
