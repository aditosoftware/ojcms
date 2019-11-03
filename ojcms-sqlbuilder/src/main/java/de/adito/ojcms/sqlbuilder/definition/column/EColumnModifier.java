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

  private final String descriptor;

  EColumnModifier()
  {
    descriptor = name();
  }

  EColumnModifier(String pDescriptor)
  {
    descriptor = pDescriptor;
  }

  @Override
  public String toStatementFormat(IDatabasePlatform pPlatform, String pIdColumnName)
  {
    return descriptor;
  }
}
