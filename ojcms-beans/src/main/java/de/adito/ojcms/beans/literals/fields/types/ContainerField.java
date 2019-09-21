package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.annotations.internal.*;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a bean container.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 09.02.2017
 */
@NeverNull
@TypeDefaultField(types = IBeanContainer.class)
@ReferenceField(resolverType = EReferableResolver.MULTI)
@GenericBeanField(genericWrapperType = IBeanContainer.class)
public class ContainerField<BEAN extends IBean<BEAN>> extends AbstractField<IBeanContainer<BEAN>>
{
  protected ContainerField(Class<IBeanContainer<BEAN>> pType, @NotNull String pName, Collection<Annotation> pAnnotations,
                           boolean pIsOptional, boolean pIsPrivate)
  {
    super(pType, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public IBeanContainer<BEAN> copyValue(IBeanContainer<BEAN> pOriginalContainer, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return IBeanContainer.ofStream(pOriginalContainer.getBeanType(), pOriginalContainer.stream()
        .map(pOriginalBean -> pMode.shouldCopyDeep() ? pOriginalBean.createCopy(pMode, pCustomFieldCopies) : pOriginalBean));
  }
}
