package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IBeanContainer;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.references.IReferable;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Beschreibt ein Bean-Feld, welche einen Bean-Container beinhaltet.
 *
 * @param <BEAN> der Typ der Beans, welche in dem Container enthalten sind
 * @author s.danner, 09.02.2017
 */
@TypeDefaultField(types = IBeanContainer.class)
public class ContainerField<BEAN extends IBean<BEAN>> extends AbstractField<IBeanContainer<BEAN>> implements IHierarchicalField<IBeanContainer<BEAN>>
{
  public ContainerField(@NotNull Class<IBeanContainer<BEAN>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public Collection<IReferable> getReferables(@Nullable IBeanContainer<BEAN> pContainer)
  {
    if (pContainer == null)
      return Collections.emptySet();
    //Alle Beans im Container
    Collection<IReferable> referables = pContainer.stream()
        .map(pBean -> (IReferable) pBean.getEncapsulated())
        .collect(Collectors.toList());
    //Und der Container selbst
    referables.add(pContainer.getEncapsulated());
    return referables;
  }
}
