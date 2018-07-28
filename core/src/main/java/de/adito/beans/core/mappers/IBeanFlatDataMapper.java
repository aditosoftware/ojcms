package de.adito.beans.core.mappers;

import de.adito.beans.core.IField;
import de.adito.beans.core.fields.FieldTuple;

import java.util.stream.Stream;

/**
 * Definition of an internal data mapper for bean values to modify the data in any way before presenting it via streams.
 * This mapper can be used to perform flat mapping operations to modify one tuple to multiple tuples.
 * This may be used to present bean values in a more detailed way in certain moments.
 *
 * @author Simon Danner, 19.03.2018
 */
public interface IBeanFlatDataMapper
{
  /**
   * The mapping operation.
   * Describes how one tuple (bean field + value) should be mapped to multiple resulting tuples.
   *
   * @param pField the original bean field
   * @param pValue the original value of the field
   * @return a stream of bean tuples
   */
  Stream<FieldTuple<?>> flatMapTuple(IField<?> pField, Object pValue);

  /**
   * Determines, if this mapping operation has been completed already.
   * This method can be used to implement iterative mapping operations.
   * By default all operations only include one step.
   *
   * @param pLastIterationChangesCount the number of changes/mappings made within the last iteration
   * @return <tt>true</tt>, if the mapping is completed
   */
  default boolean isCompleted(int pLastIterationChangesCount)
  {
    return true;
  }

  /**
   * Determines, if a certain tuple (bean field + value) should be affected by the mapping operation.
   * By default, every tuple is affected. This method can be overwritten to create more performant mapping operations.
   *
   * @param pField the field to check
   * @param pValue the value to check
   * @return <tt>true</tt>, if this tuple should be affected be the mapping operation
   */
  default boolean affectsTuple(IField<?> pField, Object pValue)
  {
    return true;
  }
}
