package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Collection;

/**
 * A bean field that holds a {@link Duration}.
 *
 * @author Simon Danner, 21.03.2020
 */
@TypeDefaultField(types = Duration.class)
public class DurationField extends AbstractField<Duration> implements ISerializableField<Duration, Long>
{
  protected DurationField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(Duration.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public Long toPersistent(Duration pValue)
  {
    return pValue.toMillis();
  }

  @Override
  public Duration fromPersistent(Long pSerialValue)
  {
    return Duration.ofMillis(pSerialValue);
  }

  @Override
  public Class<Long> getSerialValueType()
  {
    return Long.class;
  }

  @Override
  public Duration copyValue(Duration pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return Duration.ofMillis(pValue.toMillis());
  }
}
