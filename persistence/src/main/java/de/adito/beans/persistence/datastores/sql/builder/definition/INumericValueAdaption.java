package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.format.IStatementFormat;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Describes an adaption for an numeric database value based on the current value.
 * A mathematical operation will be applied to the current value.
 *
 * @param <NUMBER> the number type of the column
 * @author Simon Danner, 17.07.2018
 */
public interface INumericValueAdaption<NUMBER extends Number> extends IStatementFormat
{
  /**
   * The identification for the column that will be adapted.
   *
   * @return a column identification
   */
  IColumnIdentification<NUMBER> getColumn();

  /**
   * The mathematical operation that will be applied to the current value.
   *
   * @return a mathematical operation type
   */
  ENumericOperation getOperation();

  /**
   * The adaption number for the operation.
   *
   * @return a number for the operation
   */
  NUMBER getNumber();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumn().getColumnName() + " = " + getColumn().getColumnName() + " " + getOperation().getLiteral() + " " + getNumber();
  }

  /**
   * Creates a new instance based on required values.
   *
   * @param pColumn    the column the adaption should be applied to
   * @param pOperation the mathematical operation for the adaption
   * @param pNumber    the number for operation
   * @param <NUMBER>   the generic type for the number
   * @return a new instance based on given values
   */
  static <NUMBER extends Number> INumericValueAdaption<NUMBER> of(IColumnIdentification<NUMBER> pColumn, ENumericOperation pOperation, NUMBER pNumber)
  {
    return new INumericValueAdaption<NUMBER>()
    {
      @Override
      public IColumnIdentification<NUMBER> getColumn()
      {
        return pColumn;
      }

      @Override
      public ENumericOperation getOperation()
      {
        return pOperation;
      }

      @Override
      public NUMBER getNumber()
      {
        return pNumber;
      }
    };
  }

  /**
   * Creates an array of numeric adaptions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pCollection        a collection of source objects
   * @param pColumnResolver    a function to resolve the column identification from a source object
   * @param pOperationResolver a function to resolve the operation for the adaption from a source object
   * @param pNumberResolver    a function to resolve the number for the adaption from a source object
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of numeric adaptions
   */
  static <SOURCE> INumericValueAdaption[] ofMultiple(Collection<SOURCE> pCollection,
                                                     Function<SOURCE, IColumnIdentification<? extends Number>> pColumnResolver,
                                                     Function<SOURCE, ENumericOperation> pOperationResolver,
                                                     Function<SOURCE, ? extends Number> pNumberResolver)
  {
    return ofMultiple(pCollection.stream(), pColumnResolver, pOperationResolver, pNumberResolver);
  }

  /**
   * Creates an array of numeric adaptions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream            a stream of source objects
   * @param pColumnResolver    a function to resolve the column identification from a source object
   * @param pOperationResolver a function to resolve the operation for the adaption from a source object
   * @param pNumberResolver    a function to resolve the number for the adaption from a source object
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of numeric adaptions
   */
  static <SOURCE> INumericValueAdaption[] ofMultiple(Stream<SOURCE> pStream,
                                                     Function<SOURCE, IColumnIdentification<? extends Number>> pColumnResolver,
                                                     Function<SOURCE, ENumericOperation> pOperationResolver,
                                                     Function<SOURCE, ? extends Number> pNumberResolver)
  {
    //noinspection unchecked
    return pStream
        .map(pSource -> of((IColumnIdentification<Number>) pColumnResolver.apply(pSource), pOperationResolver.apply(pSource), pNumberResolver.apply(pSource)))
        .toArray(INumericValueAdaption[]::new);
  }
}
