package de.adito.ojcms.sqlbuilder.platform;

import de.adito.ojcms.sqlbuilder.definition.column.*;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.util.*;
import java.util.function.Function;

/**
 * Base class for {@link IDatabasePlatform}. Provides functionality to initialize the driver and mapping for columns.
 *
 * @author Simon Danner, 02.11.2019
 */
public abstract class AbstractDatabasePlatform implements IDatabasePlatform
{
  /**
   * The fully qualified driver name of the platform.
   *
   * @return the driver name of the platform
   */
  protected abstract String getDriverName();

  /**
   * Provides a platform dependent mapping to resolve the column name for each {@link EColumnType}.
   * The actual column name will be derived by an actual {@link IColumnType} instance to use size values etc.
   *
   * @return the column mapping for the platform
   */
  protected abstract Map<EColumnType, Function<IColumnType, String>> getColumnMapping();

  /**
   * Provides a map containing deviations for column modifier statement formats for this platform.
   * By default there are none. Override this method to specify some.
   *
   * @return a map containing deviating statement formats for certain column modifiers
   */
  protected Map<EColumnModifier, String> getColumnModifierDeviations()
  {
    return Collections.emptyMap();
  }

  /**
   * This method may be overwritten optionally to setup some platform specific environment.
   */
  protected void beforeDriverInit()
  {
  }

  @Override
  public void initDriver()
  {
    final String driverName = getDriverName();

    try
    {
      beforeDriverInit();
      Class.forName(driverName);
    }
    catch (ClassNotFoundException pE)
    {
      throw new OJDatabaseException("Database driver '" + driverName + "' not found!", pE);
    }
  }

  @Override
  public String columnTypeToStatementFormat(IColumnType pColumnType)
  {
    final Map<EColumnType, Function<IColumnType, String>> columnMapping = getColumnMapping();

    final EColumnType type = pColumnType.getType();
    if (!columnMapping.containsKey(type))
      throw new OJDatabaseException("No column mapping given for " + type.name() + ". (platform: " + getClass().getSimpleName() + ")");

    return columnMapping.get(type).apply(pColumnType);
  }

  @Override
  public String columnModifierToStatementFormat(EColumnModifier pModifier)
  {
    return Optional.ofNullable(getColumnModifierDeviations().get(pModifier)) //
        .orElse(pModifier.getDefaultFormat());
  }
}
