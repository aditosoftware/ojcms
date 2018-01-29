package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.references.IReferable;
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
}
