package de.adito.beans.core.mappers;

import de.adito.beans.core.fields.IField;
import de.adito.beans.core.fields.util.FieldTuple;

import java.util.stream.Stream;

/**
 * Definition of an internal data mapper for bean values to modify the data in any way before presenting it via streams.
 * This mapper affects one tuple, which leads to exactly one resulting tuple (1:1)
 * This may be used to present bean values in another format in certain situations.
 * This special mapper can be used to map a certain field only.
 *
 * @param <VALUE> the data type of the affected bean field
 * @author Simon Danner, 20.03.2018
 */
public interface ISingleFieldDataMapper<VALUE> extends ISingleFieldFlatDataMapper<VALUE>
{
  /**
   * The mapping operation.
   * Describes how the affected field and its associated value should be mapped to the resulting tuples.
   *
   * @param pField the original bean field
   * @param pValue the original value of the field
   * @return the mapped tuple
   */
  FieldTuple<VALUE> mapForField(IField<VALUE> pField, VALUE pValue);

  @Override
  default Stream<FieldTuple<VALUE>> flatMapForField(IField<VALUE> pField, VALUE pValue)
  {
    return Stream.of(mapForField(pField, pValue));
  }
}
