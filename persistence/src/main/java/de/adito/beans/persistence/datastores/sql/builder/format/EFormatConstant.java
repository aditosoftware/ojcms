package de.adito.beans.persistence.datastores.sql.builder.format;

import java.util.function.Function;

/**
 * Statement format constants to use with a {@link StatementFormatter}.
 * A format constant defines a string based statement format based on several parameters.
 * For example, the IN operator for where conditions requires a string, that lists the elements the value should be contained.
 * A constant can be used for the method {@link StatementFormatter#appendConstant(EFormatConstant, String...)}.
 * It requires a format constant and a variable amount of parameters to create the final statement format from.
 *
 * @author Simon Danner, 19.07.2018
 */
public enum EFormatConstant
{
  PRIMARY_KEY(pParams -> "PRIMARY KEY(" + pParams[0] + ")"),
  FOREIGN_KEY(pParams -> "FOREIGN KEY (" + pParams[0] + ") REFERENCES " + pParams[1] + " (" + pParams[2] + ")"),
  VALUES,
  WHERE,
  NOT(pParams -> "NOT (" + pParams[0] + ")"),
  DISTINCT,
  SET,
  COUNT(pParams -> "COUNT (" + pParams[0] + ") AS " + StaticConstants.COUNT_AS),
  IN(pParmas -> "IN (" + pParmas[0] + ")"),
  STAR(pParams -> "*");

  private final Function<String[], String> format;

  /**
   * Creates a new format constant, that uses the name of the enum as statement format.
   */
  EFormatConstant()
  {
    format = pParams -> name();
  }

  /**
   * Creates a new format constant.
   *
   * @param pFormat the function to define the statement format based on an array of parameters
   */
  EFormatConstant(Function<String[], String> pFormat)
  {
    format = pFormat;
  }

  /**
   * The format in its statement format filled with parameters.
   *
   * @param pParams the parameters to insert into the format
   * @return the statement format
   */
  public String toStatementFormat(String... pParams)
  {
    return format.apply(pParams);
  }

  /**
   * Constant holder class to use in enum constructors.
   */
  public static class StaticConstants
  {
    public static final String COUNT_AS = "rowNumber";
  }
}
