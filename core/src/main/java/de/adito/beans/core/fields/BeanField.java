package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.references.IReferable;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Beschreibt ein Feld einer Bean, welche eine Bean beinhaltet.
 *
 * @param <BEAN> der Typ der Bean, welche das Datum zu diesem Feld ist
 * @author s.danner, 09.02.2017
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
