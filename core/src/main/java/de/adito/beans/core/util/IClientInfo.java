package de.adito.beans.core.util;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Information of a client based on its zone.
 *
 * @author Simon Danner, 13.10.2017
 */
public interface IClientInfo
{
  /**
   * The client's locale.
   *
   * @return a locale
   */
  Locale getLocale();

  /**
   * The client's zone id.
   *
   * @return a zone id
   */
  ZoneId getZoneId();
}
