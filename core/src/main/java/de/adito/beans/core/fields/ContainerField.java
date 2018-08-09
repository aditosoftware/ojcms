package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.references.*;
import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A bean field that holds a bean container.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 09.02.2017
 */
@TypeDefaultField(types = IBeanContainer.class)
public class ContainerField<BEAN extends IBean<BEAN>> extends AbstractField<IBeanContainer<BEAN>> implements IHierarchicalField<IBeanContainer<BEAN>>
{
  public ContainerField(@NotNull Class<IBeanContainer<BEAN>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public IBeanContainer<BEAN> copyValue(IBeanContainer<BEAN> pOriginalContainer, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return IBeanContainer.ofStream(pOriginalContainer.getBeanType(), pOriginalContainer.stream()
        .map(pOriginalBean -> pOriginalBean.createCopy(pMode, pCustomFieldCopies)));
  }

  @Override
  public Collection<IReferable> getReferables(@Nullable IBeanContainer<BEAN> pContainer)
  {
    if (pContainer == null)
      return Collections.emptySet();
    //all beans of the container
    Collection<IReferable> referables = pContainer.stream()
        .map(pBean -> (IReferable) pBean.getEncapsulated())
        .collect(Collectors.toList());
    //plus the container itself
    referables.add(pContainer.getEncapsulated());
    return referables;
  }
}
