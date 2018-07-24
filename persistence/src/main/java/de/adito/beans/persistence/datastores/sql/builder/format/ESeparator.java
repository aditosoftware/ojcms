package de.adito.beans.persistence.datastores.sql.builder.format;

import java.util.stream.*;

/**
 * Separators for database statements.
 * They can be used to define statement with a {@link StatementFormatter}.
 *
 * @author Simon Danner, 21.07.2018
 */
public enum ESeparator
{
  COMMA(","), WHITESPACE(" "), COMMA_WITH_WHITESPACE(", "), NEW_LINE(System.lineSeparator());

  private final String literal;

  /**
   * Creates a separator type.
   *
   * @param pLiteral the string literal to separate
   */
  ESeparator(String pLiteral)
  {
    literal = pLiteral;
  }

  /**
   * The literal to separate in the statement format.
   *
   * @return the literal of this type
   */
  public String getLiteral()
  {
    return literal;
  }

  /**
   * Merges multiple separators to one string.
   *
   * @param pSeparators the separators to merge
   * @return the merged separator in a string format
   */
  public static String merge(ESeparator... pSeparators)
  {
    return Stream.of(pSeparators)
        .map(ESeparator::getLiteral)
        .collect(Collectors.joining());
  }
}
