package de.adito.ojcms.sqlbuilder.definition;

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
  public @Nullable <VALUE> String toSerial(IColumnValueTuple<VALUE> pColumnValueTuple)
  {
    final Class<VALUE> dataType = pColumnValueTuple.getColumn().getDataType();
    if (!supportedTypesToSerial.containsKey(dataType))
      throw new RuntimeException(dataType.getName() + " is not a supported value type for this serializer!");
    final VALUE value = pColumnValueTuple.getValue();
    return value == null ? null : supportedTypesToSerial.get(dataType).apply(value);
  }

  @Override
  public <VALUE> @Nullable VALUE fromSerial(IColumnIdentification<VALUE> pColumnIdentification, String pSerialValue)
  {
    final Class<VALUE> dataType = pColumnIdentification.getDataType();
    if (!supportedTypesFromSerial.containsKey(dataType))
      throw new RuntimeException(dataType.getName() + " is not a supported value type for this deserializer!");
    //noinspection unchecked
    return pSerialValue == null ? null : (VALUE) supportedTypesFromSerial.get(dataType).apply(pSerialValue);
  }

  /**
   * Adds a supported data type for this serializer.
   *
   * @param pType         the data type
   * @param pSerializer   a function to get a serial string value from an actual value
   * @param pDeserializer a function to get the actual value from the serial value
   * @param <VALUE>       the generic data type
   */
  private static <VALUE> void _put(Class<VALUE> pType, Function<VALUE, String> pSerializer, Function<String, VALUE> pDeserializer)
  {
    //noinspection unchecked
    supportedTypesToSerial.put(pType, (Function<Object, String>) pSerializer);
    supportedTypesFromSerial.put(pType, pDeserializer);
  }
}
