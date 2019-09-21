package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableFieldToString;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds an Integer.
 *
 * @author Simon Danner, 27.01.2017
 */
@NeverNull
@TypeDefaultField(types = Integer.class)
public class IntegerField extends AbstractField<Integer> implements ISerializableFieldToString<Integer>
{
  protected IntegerField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(Integer.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Integer getInitialValue()
  {
    return 0;
  }

  @Override
  public Integer copyValue(Integer pInteger, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pInteger;
  }

  @Override
  public Integer fromPersistent(String pSerialString)
  {
    return pSerialString == null ? getDefaultValue() : Integer.parseInt(pSerialString);
  }
}
