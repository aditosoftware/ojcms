package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import de.adito.beans.persistence.datastores.sql.builder.format.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * Abstract base class for any AND-OR statement concatenation, which is negatable.
 *
 * @param <NEGATE>        the concrete type of the negatable class type
 * @param <CONCATENATION> the concrete type of the concatenation class
 * @author Simon Danner, 21.07.2018
 */
abstract class AbstractStatementConcatenation<NEGATE extends IMultipleCondition<NEGATE>,
    CONCATENATION extends AbstractStatementConcatenation<NEGATE, CONCATENATION>>
    extends AbstractNegatable<NEGATE> implements IMultipleCondition<NEGATE>
{
  private final Map<IPreparedStatementFormat, EConcatenationType> concatenations = new HashMap<>();
  private IPreparedStatementFormat lastEntry;

  /**
   * Creates a new concatenation with an initial entry.
   *
   * @param pInitialEntry the first entry
   */
  protected AbstractStatementConcatenation(IPreparedStatementFormat pInitialEntry)
  {
    lastEntry = pInitialEntry;
  }

  @Override
  public String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return stream()
        .map(pCondition -> StatementFormatter.toFormat(pCondition, pDatabaseType, pIdColumnName) + " " +
            (hasConcatenation(pCondition) ? getConcatenationType(pCondition).name() + " " : ""))
        .collect(Collectors.joining());
  }

  @NotNull
  @Override
  public Iterator<IPreparedStatementFormat> iterator()
  {
    return Stream.concat(concatenations.keySet().stream(), Stream.of(lastEntry)).iterator();
  }

  /**
   * Adds an statement format to the concatenation.
   * The new element will be the last entry.
   * The old last entry will be stored in the map with the according concatenation type.
   *
   * @param pFormat            the format to add
   * @param pConcatenationType the concatenation type for the last condition
   * @return the concatenation itself for a pipelining mechanism
   */
  protected CONCATENATION addConcatenation(IPreparedStatementFormat pFormat, EConcatenationType pConcatenationType)
  {
    concatenations.put(lastEntry, pConcatenationType);
    lastEntry = pFormat;
    //noinspection unchecked
    return (CONCATENATION) this;
  }

  /**
   * Determines, if a certain key has a concatenation.
   * Concatenations are considered to be put after the format.
   *
   * @param pFormat the format to check
   * @return <tt>true</tt>, if the statement format has a concatenation
   */
  protected boolean hasConcatenation(IPreparedStatementFormat pFormat)
  {
    return concatenations.containsKey(pFormat);
  }

  /**
   * The concatenation type for a certain statement format.
   * Concatenations are considered to be put after the format.
   *
   * @param pFormat the format to get the concatenation type for
   * @return the concatenation type
   */
  protected EConcatenationType getConcatenationType(IPreparedStatementFormat pFormat)
  {
    return concatenations.get(pFormat);
  }
}
