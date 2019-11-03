package de.adito.ojcms.sqlbuilder.platform;

import de.adito.ojcms.sqlbuilder.platform.derby.DerbyPlatform;

import java.util.function.Supplier;

/**
 * Lists all available external database platforms.
 *
 * @author Simon Danner, 02.11.2019
 */
public enum EExternalDatabasePlatform
{
  DERBY(DerbyPlatform::new);

  private final IExternalDatabasePlatform platform;

  EExternalDatabasePlatform(Supplier<IExternalDatabasePlatform> pPlatformSupplier)
  {
    platform = pPlatformSupplier.get();
  }

  public IExternalDatabasePlatform platform()
  {
    return platform;
  }
}
