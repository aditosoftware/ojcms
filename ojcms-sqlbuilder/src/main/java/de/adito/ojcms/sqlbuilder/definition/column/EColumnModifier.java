package de.adito.ojcms.sqlbuilder.definition.column;

import de.adito.ojcms.sqlbuilder.format.IStatementFormat;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;

/**
 * All possible database column modifiers.
 *
 * @author Simon Danner, 18.05.2018
 */
public enum EColumnModifier implements IStatementFormat
{
  NOT_NULL("NOT NULL"), AUTO_INCREMENT, UNIQUE;

  private final String format;

  EColumnModifier()
  {
    format = name();
  }

  EColumnModifier(String pFormat)
  {
    format = pFormat;
  }

  public String getDefaultFormat()
  {
    return format;
  }

  @Override
  public String toStatementFormat(IDatabasePlatform pPlatform, String pIdColumnName)
  {
    return pPlatform.columnModifierToStatementFormat(this);
  }
}
