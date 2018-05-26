package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.references.*;
import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field that holds a bean.
 *
 * @param <BEAN> the generic type of the bean that this field is referring to
 * @author Simon Danner, 09.02.2017
 */
@TypeDefaultField(types = IBean.class)
public class BeanField<BEAN extends IBean<BEAN>> extends AbstractField<BEAN> implements IHierarchicalField<BEAN>
{
  public BeanField(@NotNull Class<BEAN> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public Collection<IReferable> getReferables(@Nullable BEAN pBean)
  {
    return pBean != null ? Collections.singleton(pBean.getEncapsulated()) : Collections.emptySet();
  }

  @Override
  public BEAN copyValue(BEAN pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue.createCopy(true, pCustomFieldCopies);
  }
}
