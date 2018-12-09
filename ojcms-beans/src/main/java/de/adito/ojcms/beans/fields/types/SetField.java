package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.fields.serialization.ISerializableFieldJson;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link Set}.
 *
 * @param <ELEMENT> the type of the elements in the set
 * @author Simon Danner, 01.08.2018
 */
public class SetField<ELEMENT> extends AbstractField<Set<ELEMENT>> implements ISerializableFieldJson<Set<ELEMENT>>
{
  public SetField(@NotNull Class<Set<ELEMENT>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }
}
