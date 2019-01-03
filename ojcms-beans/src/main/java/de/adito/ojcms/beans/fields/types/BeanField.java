package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.GenericBeanField;
import de.adito.ojcms.beans.annotations.internal.*;
import de.adito.ojcms.beans.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a bean.
 *
 * @param <BEAN> the generic type of the bean that this field is referring to
 * @author Simon Danner, 09.02.2017
 */
@TypeDefaultField(types = IBean.class)
@ReferenceField(resolverType = EReferableResolver.SINGLE)
@GenericBeanField
public class BeanField<BEAN extends IBean<BEAN>> extends AbstractField<BEAN>
{
  protected BeanField(Class<BEAN> pType, @NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional)
  {
    super(pType, pName, pAnnotations, pIsOptional);
  }

  @Override
  public BEAN copyValue(BEAN pBean, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pBean.createCopy(pMode, pCustomFieldCopies);
  }
}
