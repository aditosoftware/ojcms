package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a character.
 *
 * @author Simon Danner, 07.09.2017
 */
@TypeDefaultField(types = Character.class)
public class CharacterField extends AbstractField<Character> implements ISerializableField<Character>
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
  public Character copyValue(Character pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue;
  }

  @Override
  public Character fromPersistent(String pSerialString)
  {
    return pSerialString == null ? null : pSerialString.charAt(0);
  }
}
