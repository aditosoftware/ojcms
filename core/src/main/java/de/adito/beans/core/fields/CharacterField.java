package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a character.
 *
 * @author Simon Danner, 07.09.2017
 */
@TypeDefaultField(types = Character.class)
public class CharacterField extends AbstractField<Character>
{
  public CharacterField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Character.class, pName, pAnnotations);
  }
}
