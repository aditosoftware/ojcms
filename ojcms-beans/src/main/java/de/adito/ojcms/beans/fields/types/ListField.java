package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.fields.serialization.ISerializableFieldJson;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A bean field for a {@link List}.
 *
 * @param <ELEMENT> the type of the elements in the list
 * @author Simon Danner, 01.08.2018
 */
@TypeDefaultField(types = List.class)
public class ListField<ELEMENT> extends AbstractField<List<ELEMENT>> implements ISerializableFieldJson<List<ELEMENT>>
{
  public ListField(@NotNull Class<List<ELEMENT>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  @Override
  public List<ELEMENT> getInitialValue()
  {
    return Collections.emptyList();
  }
}
