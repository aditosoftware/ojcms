package de.adito.beans.core.util;

import java.time.ZoneId;
import java.util.Locale;

/**
 * @author s.danner, 13.10.2017
 */
public interface IClientInfo
{
  Locale getLocale();

  ZoneId getZoneId();
}
