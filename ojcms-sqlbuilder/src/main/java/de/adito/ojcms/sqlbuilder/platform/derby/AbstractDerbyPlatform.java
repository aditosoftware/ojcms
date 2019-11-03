package de.adito.ojcms.sqlbuilder.platform.derby;

import de.adito.ojcms.sqlbuilder.definition.column.*;
import de.adito.ojcms.sqlbuilder.platform.AbstractDatabasePlatform;

import java.util.*;
import java.util.function.Function;

/**
 * Base platform information for DERBY database systems.
 *
 * @author Simon Danner, 02.11.2019
 */
abstract class AbstractDerbyPlatform extends AbstractDatabasePlatform
{
  private static final Map<EColumnType, Function<IColumnType, String>> COLUMN_MAPPING = new HashMap<>();

  static
  {
    COLUMN_MAPPING.put(EColumnType.LONG, pType -> "BIGINT");
    COLUMN_MAPPING.put(EColumnType.BLOB, pType -> "BLOB");
    COLUMN_MAPPING.put(EColumnType.CHAR, pType -> "CHAR(1)");
    COLUMN_MAPPING.put(EColumnType.DATE, pType -> "DATE");
    COLUMN_MAPPING.put(EColumnType.DATETIME, pType -> "DATETIME");
    COLUMN_MAPPING.put(EColumnType.DOUBLE, pType -> "DOUBLE");
    COLUMN_MAPPING.put(EColumnType.FLOAT, pType -> "FLOAT");
    COLUMN_MAPPING.put(EColumnType.INT, pType -> "INTEGER");
    COLUMN_MAPPING.put(EColumnType.SHORT, pType -> "SHORT");
    COLUMN_MAPPING.put(EColumnType.TIME, pType -> "TIME");
    COLUMN_MAPPING.put(EColumnType.STRING, pType -> "VARCHAR(" + (pType.hasLength() ? pType.getLength() : 255) + ")");
  }

  @Override
  public String getPlatformName()
  {
    return "Derby";
  }

  @Override
  public String getSystemTablePrefix()
  {
    return "SYS";
  }

  @Override
  protected Map<EColumnType, Function<IColumnType, String>> getColumnMapping()
  {
    return COLUMN_MAPPING;
  }
}
