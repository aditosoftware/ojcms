package de.adito.beans.core.mappers;

import de.adito.beans.core.IField;
import de.adito.beans.core.fields.FieldTuple;

import java.util.stream.Stream;

/**
 * Definition of an internal data mapper for bean values to modify the data in any way before presenting it via streams.
 * This mapper can be used to perform flat mapping operations to modify one tuple to multiple tuples.
 * This may be used to present bean values in a more detailed way in certain moments.
 * This special mapper can be used to map a certain field only.
 *
 * @param <TYPE> the data type of the affected bean field
 * @author Simon Danner, 20.03.2018
 */
public interface ISingleFieldFlatDataMapper<TYPE> extends IBeanFlatDataMapper
{
  /**
   * The mapping operation.
   * Describes how the affected field and its associated value should be mapped to multiple resulting tuples.
   *
   * @param pField the original bean field
   * @param pValue the original value of the field
   * @return a stream of bean tuples
   */
  Stream<FieldTuple<TYPE>> flatMapForField(IField<TYPE> pField, TYPE pValue);

  @Override
  default Stream<FieldTuple<?>> flatMapTuple(IField<?> pField, Object pValue)
  {
    //noinspection unchecked
    return flatMapForField((IField<TYPE>) pField, (TYPE) pValue)
        .map(pTuple -> pTuple);
  }
}
