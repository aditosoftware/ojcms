package de.adito.ojcms.sqlbuilder.definition;

/**
 * Enumerates mathematical operations for database statements.
 *
 * @author Simon Danner, 17.07.2018
 */
public enum ENumericOperation
{
  ADD('+'), SUBTRACT('-'), MULTIPLY('*'), DIVIDE('/');

  private final char literal;

  /**
   * Creates a new numeric operation type.
   *
   * @param pLiteral the literal of the operation
   */
  ENumericOperation(char pLiteral)
  {
    literal = pLiteral;
  }

  /**
   * The literal of this numeric operation.
   *
   * @return the literal
   */
  public char getLiteral()
  {
    return literal;
  }
}
