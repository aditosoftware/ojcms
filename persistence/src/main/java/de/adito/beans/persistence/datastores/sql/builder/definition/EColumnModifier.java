package de.adito.beans.persistence.datastores.sql.builder.definition;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * All possible database column modifiers.
 *
 * @author Simon Danner, 18.05.2018
 */
public enum EColumnModifier
{
  NOT_NULL("NOT NULL"), PRIMARY_KEY, AUTO_INCREMENT, UNIQUE;

  private final String descriptor;

  EColumnModifier()
  {
    descriptor = name();
  }

  EColumnModifier(String pDescriptor)
  {
    descriptor = pDescriptor;
  }

  /**
   * The descriptor of this modifier.
   *
   * @return a descriptor for the modifier
   */
  public String getDescriptor()
  {
    return descriptor;
  }

  /**
   * Concatenates an array of modifiers to one string to use for a SQL query.
   *
   * @param pModifiers the array of modifiers
   * @return the modifiers as a single string
   */
  public static String asString(Collection<EColumnModifier> pModifiers)
  {
    return pModifiers.isEmpty() ? "" : " " + pModifiers.stream()
        .filter(pModifier -> pModifier != PRIMARY_KEY)
        .map(EColumnModifier::getDescriptor)
        .collect(Collectors.joining(" "));
  }
}
