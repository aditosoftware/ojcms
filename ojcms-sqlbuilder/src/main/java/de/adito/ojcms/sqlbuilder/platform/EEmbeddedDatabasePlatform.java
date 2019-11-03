package de.adito.ojcms.sqlbuilder.platform;

import de.adito.ojcms.sqlbuilder.platform.derby.DerbyEmbeddedPlatform;

import java.util.function.Supplier;

/**
 * Lists all available embedded database platforms.
 *
 * @author Simon Danner, 02.11.2019
 */
public enum EEmbeddedDatabasePlatform
{
  DERBY(DerbyEmbeddedPlatform::new);

  private final IEmbeddedDatabasePlatform platform;

  EEmbeddedDatabasePlatform(Supplier<IEmbeddedDatabasePlatform> pPlatformSupplier)
  {
    platform = pPlatformSupplier.get();
  }

  public IEmbeddedDatabasePlatform getPlatform()
  {
    return platform;
  }
}
