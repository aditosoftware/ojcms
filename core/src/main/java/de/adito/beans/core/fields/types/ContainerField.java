package de.adito.beans.core.fields.types;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.internal.*;
import de.adito.beans.core.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a bean container.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 09.02.2017
 */
@TypeDefaultField(types = IBeanContainer.class)
@ReferenceField(resolverType = EReferableResolver.MULTI)
public class ContainerField<BEAN extends IBean<BEAN>> extends AbstractField<IBeanContainer<BEAN>>
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
}
