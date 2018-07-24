package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import de.adito.beans.persistence.datastores.sql.builder.format.IStatementFormat;

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
  public String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return descriptor;
  }
}
