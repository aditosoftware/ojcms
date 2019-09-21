package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableFieldJson;
import de.adito.ojcms.beans.literals.fields.util.ISneakyCopyCreatorField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link Set}.
 *
 * @param <ELEMENT> the type of the elements in the set
 * @author Simon Danner, 01.08.2018
 */
@NeverNull
@TypeDefaultField(types = Set.class)
@GenericBeanField(genericWrapperType = Set.class)
public class SetField<ELEMENT> extends AbstractField<Set<ELEMENT>>
    implements ISerializableFieldJson<Set<ELEMENT>>, ISneakyCopyCreatorField<Set<ELEMENT>>
{
  protected SetField(Class<Set<ELEMENT>> pType, @NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional,
                     boolean pIsPrivate)
  {
    super(pType, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Set<ELEMENT> getInitialValue()
  {
    return Collections.emptySet();
  }
}
