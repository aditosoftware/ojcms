package de.adito.beans.core.fields.types;

import de.adito.beans.core.fields.serialization.ISerializableFieldJson;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link List}.
 *
 * @param <ELEMENT> the type of the elements in the list
 * @author Simon Danner, 01.08.2018
 */
public class ListField<ELEMENT> extends AbstractField<List<ELEMENT>> implements ISerializableFieldJson<List<ELEMENT>>
{
  public ListField(@NotNull Class<List<ELEMENT>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }
}
