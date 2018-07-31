package de.adito.beans.persistence.datastores.sql.builder.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * A statement format builder to define SQL statements in a functional way.
 * Instances can be created via {@link EFormatter}.
 * Several append methods can be used to build the different statements.
 * Most of the appended elements are divided by a whitespace.
 * For further usage information check the methods below.
 *
 * The result of this formatter can be retrieved via {@link #getStatement()} and {@link #getSerialArguments(IValueSerializer)},
 * which both provide information for a prepared statement. This formatter generally allows normal and prepared statement formats (see methods)
 *
 * @author Simon Danner, 19.07.2018
 * @see EFormatter
 * @see EFormatConstant
 * @see ESeparator
 */
public class StatementFormatter
{
  //These strings will not be followed by a whitespace
  private static final Set<String> noWhitespace = new HashSet<>(Arrays.asList("(", System.lineSeparator()));

  private final EDatabaseType databaseType;
  private final String idColumnName;
  private final _Builder builder;
  private final String tableNamePrefix;
  private final List<IColumnValueTuple<?>> arguments = new ArrayList<>();

  /**
   * Creates a new statement formatter.
   *
   * @param pDatabaseType    the database type used for the statements
   * @param pIdColumnName    the global id column name used for the statements
   * @param pStatementName   the name of the statement (SELECT, INSERT, etc)
   * @param pTableNamePrefix the table name prefix of the statement type (e.g. 'FROM' for SELECT)
   */
  StatementFormatter(EDatabaseType pDatabaseType, String pIdColumnName, String pStatementName, String pTableNamePrefix)
  {
    databaseType = pDatabaseType;
    idColumnName = pIdColumnName;
    tableNamePrefix = pTableNamePrefix;
    builder = new _Builder(pStatementName);
  }

  /**
   * A static helper method to transform a statement format instance into the string based format.
   * This method checks, if the statement format is negatable and adds 'NOT' accordingly.
   *
   * @param pFormat       the statement format to transform
   * @param pDatabaseType the database type used for the statements
   * @param pIdColumnName the global id column name used for the statements
   * @return the statement in its string format
   */
  public static String toFormat(IStatementFormat pFormat, EDatabaseType pDatabaseType, String pIdColumnName)
  {
    final String format = pFormat.toStatementFormat(pDatabaseType, pIdColumnName);
    return pFormat instanceof INegatable && ((INegatable) pFormat).isNegated() ? EFormatConstant.NOT.toStatementFormat(format) : format;
  }

  /**
   * A static helper method to join multiple string with several {@link ESeparator}.
   *
   * @param pMultipleStrings a stream of strings to concatenate
   * @param pSeparators      the separators for the single elements
   * @return the concatenated string
   */
  public static String join(Stream<String> pMultipleStrings, ESeparator... pSeparators)
  {
    return pMultipleStrings
        .collect(Collectors.joining(ESeparator.merge(pSeparators)));
  }

  /**
   * Appends something to the formatter via a consumer of this formatter.
   * This mechanism allows the user to stay in the pipeline.
   *
   * @param pFormatterConsumer a consumer of this formatter, that appends the elements
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendFunctional(Consumer<StatementFormatter> pFormatterConsumer)
  {
    pFormatterConsumer.accept(this);
    return this;
  }

  /**
   * Appends something to the formatter, if a certain condition is true.
   *
   * @param pCondition         the condition to determine, if the elements should be appended
   * @param pFormatterConsumer a consumer of this formatter, that appends the elements
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter conditional(boolean pCondition, Consumer<StatementFormatter> pFormatterConsumer)
  {
    if (pCondition)
      pFormatterConsumer.accept(this);
    return this;
  }

  /**
   * Appends something to the formatter, if a certain condition is true and appends something else otherwise.
   *
   * @param pCondition the condition to determine, if the elements of the true case should be appended
   * @param pTrueCase  a consumer of this formatter, that appends the elements in the true case
   * @param pFalseCase a consumer of this formatter, that appends the elements in the false case
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter conditionalOrElse(boolean pCondition, Consumer<StatementFormatter> pTrueCase,
                                              Consumer<StatementFormatter> pFalseCase)
  {
    if (pCondition)
      pTrueCase.accept(this);
    else
      pFalseCase.accept(this);
    return this;
  }

  /**
   * Appends the table name.
   * The predefined table name prefix will be added before.
   *
   * @param pTableName the table name to append
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendTableName(String pTableName)
  {
    return builder.appendWithWhitespace(tableNamePrefix).builder.appendWithWhitespace(pTableName);
  }

  /**
   * Appends a format constant.
   * The format constant may require several parameters.
   *
   * @param pConstant the format constant to append
   * @param pParams   the parameters for the constant
   * @return this formatter itself to enable a pipelining mechanism
   * @see EFormatConstant
   */
  public StatementFormatter appendConstant(EFormatConstant pConstant, String... pParams)
  {
    return builder.appendWithWhitespace(pConstant.toStatementFormat(pParams));
  }

  /**
   * Appends a variable amount of separators.
   *
   * @param pSeparators the separators
   * @return this formatter itself to enable a pipelining mechanism
   * @see ESeparator
   */
  public StatementFormatter appendSeparator(ESeparator... pSeparators)
  {
    return builder.appendDirectly(ESeparator.merge(pSeparators));
  }

  /**
   * Appends a statement format instance.
   *
   * @param pFormat the statement format to append
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendStatement(IStatementFormat pFormat)
  {
    return _appendFormat(pFormat);
  }

  /**
   * Appends a prepared statement format instance.
   * The arguments of the format will be collected.
   *
   * @param pFormat the prepared statement format to append
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendPreparedStatement(IPreparedStatementFormat pFormat)
  {
    arguments.addAll(pFormat.getArguments(databaseType, idColumnName));
    return _appendFormat(pFormat);
  }

  /**
   * Appends multiple statement format instances separated by several separators.
   *
   * @param pFormatStream a stream of statement formats to append
   * @param pSeparators   the separators to divide the single formats
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendMultiple(Stream<? extends IStatementFormat> pFormatStream, ESeparator... pSeparators)
  {
    return _appendMultiple(pFormatStream.map(pFormat -> toFormat(pFormat, databaseType, idColumnName)), pSeparators);
  }

  /**
   * Appends multiple prepared statement format instances separated by several separators.
   * The arguments of the formats will be collected.
   *
   * @param pFormatStream a stream of prepared statement formats to append
   * @param pSeparators   the separators to divide the single formats
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendMultiplePrepared(Stream<? extends IPreparedStatementFormat> pFormatStream, ESeparator... pSeparators)
  {
    return _appendMultiple(pFormatStream
                               .peek(pFormat -> arguments.addAll(pFormat.getArguments(databaseType, idColumnName)))
                               .map(pFormat -> toFormat(pFormat, databaseType, idColumnName)), pSeparators);
  }

  /**
   * Appends an enumeration of multiple (prepared) arguments.
   * For every argument a '?' divided by the given separators will be added to the format.
   * The arguments will be added to the prepared argument collection.
   * A format example with a comma separator would be: "?, ?, ?, ?".
   * This could be used for the 'VALUES' clause of an insert statement.
   *
   * @param pArguments  a list of column value tuples as arguments
   * @param pSeparators the separators for each argument
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendMultipleArgumentEnumeration(List<IColumnValueTuple<?>> pArguments, ESeparator... pSeparators)
  {
    arguments.addAll(pArguments);
    return _appendMultiple(IntStream.range(0, pArguments.size()).mapToObj(pIndex -> "?"), pSeparators);
  }

  /**
   * Appends an enumeration of several strings concatenated by certain separators.
   *
   * @param pElements   the strings to join/enumerate
   * @param pSeparators the separators to divide the single elements
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendEnumeration(Stream<String> pElements, ESeparator... pSeparators)
  {
    return _appendMultiple(pElements, pSeparators);
  }

  /**
   * Appends the where condition of a statement.
   *
   * @param pModifiers the where modifiers of a statement
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter appendWhereCondition(WhereModifiers pModifiers)
  {
    final Optional<IPreparedStatementFormat> where = pModifiers.where();
    if (where.isPresent())
    {
      builder.appendWithWhitespace("WHERE");
      appendPreparedStatement(where.get());
    }
    return this;
  }

  /**
   * Appends an opened bracket.
   *
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter openBracket()
  {
    return builder.appendWithWhitespace("(");
  }

  /**
   * Appends a closed bracket.
   *
   * @return this formatter itself to enable a pipelining mechanism
   */
  public StatementFormatter closeBracket()
  {
    return builder.appendDirectly(")");
  }

  /**
   * The whole statement created by this formatter.
   *
   * @return the final database statement
   */
  public String getStatement()
  {
    return builder.toString();
  }

  /**
   * A list of all collected arguments for prepared statements.
   * They are in the input order.
   * The arguments are converted to a serial string format.
   *
   * @param pSerializer a value serializer to convert the arguments in a serial format for the prepared statements
   * @return a list of serial arguments of the statement created by this formatter
   */
  public List<String> getSerialArguments(IValueSerializer pSerializer)
  {
    return arguments.stream()
        .map(pSerializer::toSerial)
        .collect(Collectors.toList());
  }

  /**
   * Appends a statement format instance to the internal builder.
   *
   * @param pFormat the statement format to append
   * @return this formatter itself
   */
  private StatementFormatter _appendFormat(IStatementFormat pFormat)
  {
    return builder.appendWithWhitespace(toFormat(pFormat, databaseType, idColumnName));
  }

  /**
   * Appends multiple strings divided by several separators to the internal builder.
   *
   * @param pStreamOfStrings a stream of strings to append/concatenate
   * @param pSeparators      the separators to divide the single strings
   * @return this formatter itself
   */
  private StatementFormatter _appendMultiple(Stream<String> pStreamOfStrings, ESeparator... pSeparators)
  {
    return builder.appendWithWhitespace(join(pStreamOfStrings, pSeparators));
  }

  /**
   * Defines an internal string builder for the statement.
   * This builder adds whitespaces after every appended element, except for some 'blacklisted' characters.
   * It also provides a method to append a string directly, which is necessary in some cases.
   */
  private class _Builder
  {
    private final StringBuilder stringBuilder;
    private String lastAddition;

    /**
     * Creates the internal builder.
     *
     * @param pStatementName the name of the statement to create
     */
    private _Builder(String pStatementName)
    {
      stringBuilder = new StringBuilder(pStatementName);
      lastAddition = pStatementName;
    }

    /**
     * Appends a string format with a leading whitespace.
     * If the lastAddition ends with 'blacklisted' characters, no whitespace will be added. (see the static set at the top)
     *
     * @param pFormat the string format to append
     * @return the statement formatter itself
     */
    public StatementFormatter appendWithWhitespace(String pFormat)
    {
      if (noWhitespace.stream().noneMatch(pForbiddenLastEntry -> lastAddition.endsWith(pForbiddenLastEntry)))
        stringBuilder.append(' ');
      return appendDirectly(pFormat);
    }

    /**
     * Adds a string format directly without a whitespace.
     *
     * @param pFormat the string format to append
     * @return the statement formatter itself
     */
    public StatementFormatter appendDirectly(String pFormat)
    {
      lastAddition = pFormat;
      stringBuilder.append(pFormat);
      return StatementFormatter.this;
    }

    @Override
    public String toString()
    {
      return stringBuilder.toString();
    }
  }
}
