package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.IAutoSerializableField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a long.
 *
 * @author Simon Danner, 14.02.2017
 */
@NeverNull
@TypeDefaultField(types = Long.class)
public class LongField extends AbstractField<Long> implements IAutoSerializableField<Long>
{
  public LongField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(Long.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Long getInitialValue()
  {
    return 0L;
  }

  @Override
  public Long copyValue(Long pLong, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pLong;
  }
}
