package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.util.IClientInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * A bean field that holds a date (as {@link Instant}).
 * Provides converters for {@link Date} and {@link Long}.
 *
 * @author Simon Danner, 23.08.2016
 */
@TypeDefaultField(types = {Instant.class, Date.class})
public class DateField extends AbstractField<Instant>
{
  public DateField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(Instant.class, pName, pAnnotations);
    registerConverter(Date.class, Date::toInstant, Date::from);
    registerConverter(Long.class, Instant::ofEpochMilli, Instant::toEpochMilli);
  }

  @Override
  public String display(Instant pValue, IClientInfo pClientInfo)
  {
    ZonedDateTime date = ZonedDateTime.ofInstant(pValue, pClientInfo.getZoneId());
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
        .withLocale(pClientInfo.getLocale())
        .format(date);
  }
}
