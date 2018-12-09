package de.adito.ojcms.beans.fields.types;

import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.fields.serialization.ISerializableFieldToString;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a character.
 *
 * @author Simon Danner, 07.09.2017
 */
@TypeDefaultField(types = Character.class)
public class CharacterField extends AbstractField<Character> implements ISerializableFieldToString<Character>
{
  public CharacterField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Character.class, pName, pAnnotations);
  }

  @Override
  public Character getDefaultValue()
  {
    return '\u0000';
  }

  @Override
  public Character getInitialValue()
  {
    return '\u0000';
  }

  @Override
  public Character copyValue(Character pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }

  @Override
  public Character fromPersistent(String pSerialString)
  {
    return pSerialString == null ? null : pSerialString.charAt(0);
  }
}
