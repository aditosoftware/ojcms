package de.adito.beans.core.mappers;

import de.adito.beans.core.IField;
import de.adito.beans.core.fields.FieldTuple;

import java.util.stream.Stream;

/**
 * Definition of an internal data mapper for bean values to modify the data in any way before presenting it via streams.
 * This mapper affects one tuple, which leads to exactly one resulting tuple (1:1)
 * This may be used to present bean values in another format in certain situations.
 *
 * @author Simon Danner, 19.03.2018
 */
public interface IBeanDataMapper extends IBeanFlatDataMapper
{
  /**
   * The mapping operation.
   * Describes how one tuple (bean field + value) should be mapped to the resulting tuple.
   *
   * @param pField the original bean field
   * @param pValue the original value of the field
   * @return the mapped bean tuple
   */
  FieldTuple<?> mapTuple(IField<?> pField, Object pValue);

  @Override
  default Stream<FieldTuple<?>> flatMapTuple(IField<?> pField, Object pValue)
  {
    return Stream.of(mapTuple(pField, pValue));
  }
}
