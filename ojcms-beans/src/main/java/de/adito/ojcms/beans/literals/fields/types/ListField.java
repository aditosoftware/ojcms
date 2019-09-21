package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableFieldJson;
import de.adito.ojcms.beans.literals.fields.util.ISneakyCopyCreatorField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link List}.
 *
 * @param <ELEMENT> the type of the elements in the list
 * @author Simon Danner, 01.08.2018
 */
@NeverNull
@TypeDefaultField(types = List.class)
@GenericBeanField(genericWrapperType = List.class)
public class ListField<ELEMENT> extends AbstractField<List<ELEMENT>>
    implements ISerializableFieldJson<List<ELEMENT>>, ISneakyCopyCreatorField<List<ELEMENT>>
{
  protected ListField(Class<List<ELEMENT>> pType, @NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional,
                      boolean pIsPrivate)
  {
    super(pType, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public List<ELEMENT> getInitialValue()
  {
    return Collections.emptyList();
  }
}
