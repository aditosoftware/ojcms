package de.adito.beans.persistence.datastores.sql.builder.definition;

import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * Default value serialization implementation.
 * It mainly supports all primitive types and strings.
 *
 * @author Simon Danner, 18.06.2018
 */
class DefaultValueSerializer implements IValueSerializer
{
  private static Map<Class, Function<Object, String>> supportedTypesToSerial = new HashMap<>();
  private static Map<Class, Function<String, ?>> supportedTypesFromSerial = new HashMap<>();

  /*
   * Adds all supported data types.
   * May be extended in the future.
   */
  static
  {
    _put(Character.class, String::valueOf, pValue -> pValue.charAt(0));
    _put(String.class, pValue -> pValue, pValue -> pValue);
    _put(Integer.class, String::valueOf, Integer::parseInt);
    _put(Double.class, String::valueOf, Double::parseDouble);
    _put(Float.class, String::valueOf, Float::parseFloat);
    _put(Short.class, String::valueOf, Short::parseShort);
    _put(Long.class, String::valueOf, Long::parseLong);
    _put(Boolean.class, String::valueOf, Boolean::parseBoolean);
    _put(LocalDate.class, LocalDate::toString, LocalDate::parse);
    _put(LocalDateTime.class, LocalDateTime::toString, LocalDateTime::parse);
    _put(LocalTime.class, LocalTime::toString, LocalTime::parse);
  }

  @Override
  public @Nullable <TYPE> String toSerial(IColumnValueTuple<TYPE> pColumnValueTuple)
  {
    Class<TYPE> dataType = pColumnValueTuple.getColumn().getDataType();
    if (!supportedTypesToSerial.containsKey(dataType))
      throw new RuntimeException(dataType.getName() + " is not a supported value type for this serializer!");
    TYPE value = pColumnValueTuple.getValue();
    return value == null ? null : supportedTypesToSerial.get(dataType).apply(value);
  }

  @Override
  public <TYPE> @Nullable TYPE fromSerial(IColumnIdentification<TYPE> pColumnIdentification, String pSerialValue)
  {
    Class<TYPE> dataType = pColumnIdentification.getDataType();
    if (!supportedTypesFromSerial.containsKey(dataType))
      throw new RuntimeException(dataType.getName() + " is not a supported value type for this deserializer!");
    //noinspection unchecked
    return pSerialValue == null ? null : (TYPE) supportedTypesFromSerial.get(dataType).apply(pSerialValue);
  }

  /**
   * Adds a supported data type for this serializer.
   *
   * @param pType         the data type
   * @param pSerializer   a function to get a serial string value from an actual value
   * @param pDeserializer a function to get the actual value from the serial value
   * @param <TYPE>        the generic data type
   */
  private static <TYPE> void _put(Class<TYPE> pType, Function<TYPE, String> pSerializer, Function<String, TYPE> pDeserializer)
  {
    //noinspection unchecked
    supportedTypesToSerial.put(pType, (Function<Object, String>) pSerializer);
    supportedTypesFromSerial.put(pType, pDeserializer);
  }
}
