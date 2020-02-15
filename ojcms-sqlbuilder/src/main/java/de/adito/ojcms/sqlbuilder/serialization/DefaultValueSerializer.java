package de.adito.ojcms.sqlbuilder.serialization;

import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;

import static java.util.function.Function.identity;

/**
 * Default value serialization implementation.
 * It mainly supports all primitive types, strings and date types.
 *
 * @author Simon Danner, 18.06.2018
 */
public class DefaultValueSerializer implements IValueSerializer
{
  private static final Map<Class, _SupportedSerialization<?, ?>> SUPPORTED_SERIALIZATIONS = new HashMap<>();
  protected static final ISerialValue NULL_SERIAL_VALUE = new _NullSerialValue();

  /*
   * Adds all supported data types.
   * May be extended in the future.
   */
  static
  {
    _put(Character.class, String::valueOf, (pStatement, pSerial, pIndex) -> pStatement.setString(pIndex, pSerial), ResultSet::getString,
         pSerial -> pSerial.charAt(0));
    _put(String.class, (pStatement, pSerial, pIndex) -> pStatement.setString(pIndex, pSerial), ResultSet::getString);
    _put(Integer.class, (pStatement, pSerial, pIndex) -> pStatement.setInt(pIndex, pSerial), ResultSet::getInt);
    _put(Double.class, (pStatement, pSerial, pIndex) -> pStatement.setDouble(pIndex, pSerial), ResultSet::getDouble);
    _put(Float.class, (pStatement, pSerial, pIndex) -> pStatement.setFloat(pIndex, pSerial), ResultSet::getFloat);
    _put(Short.class, (pStatement, pSerial, pIndex) -> pStatement.setShort(pIndex, pSerial), ResultSet::getShort);
    _put(Long.class, (pStatement, pSerial, pIndex) -> pStatement.setLong(pIndex, pSerial), ResultSet::getLong);
    _put(Boolean.class, (pStatement, pSerial, pIndex) -> pStatement.setBoolean(pIndex, pSerial), ResultSet::getBoolean);
    _put(byte[].class, (pStatement, pSerial, pIndex) -> pStatement.setBytes(pIndex, pSerial), ResultSet::getBytes);
    _put(LocalDate.class, LocalDate::toEpochDay, (pStatement, pSerial, pIndex) -> pStatement.setLong(pIndex, pSerial), ResultSet::getLong,
         LocalDate::ofEpochDay);
    _put(LocalTime.class, LocalTime::toNanoOfDay, (pStatement, pSerial, pIndex) -> pStatement.setLong(pIndex, pSerial), ResultSet::getLong,
         LocalTime::ofNanoOfDay);
    _put(Instant.class, Instant::toEpochMilli, (pStatement, pSerial, pIndex) -> pStatement.setLong(pIndex, pSerial), ResultSet::getLong,
         Instant::ofEpochMilli);
  }

  @Override
  @Nullable
  public <VALUE> ISerialValue toSerial(IColumnValueTuple<VALUE> pColumnValueTuple)
  {
    final Class<VALUE> dataType = pColumnValueTuple.getColumn().getDataType();
    return createSerialValue(dataType, pColumnValueTuple.getValue());
  }

  @Override
  @Nullable
  public <VALUE> VALUE fromSerial(IColumnIdentification<VALUE> pColumn, ResultSet pResultSet, int pIndex)
  {
    return retrieveSerialValue(pColumn.getDataType(), pResultSet, pIndex);
  }

  /**
   * Creates a {@link ISerialValue} for a specific data type and an actual instance of this type.
   *
   * @param pDataType  the data type of the serial value
   * @param pDataValue the data value to serialize
   * @return the created serial value
   */
  @Nullable
  protected <VALUE, SERIAL extends Serializable> ISerialValue createSerialValue(Class<VALUE> pDataType, VALUE pDataValue)
  {
    if (pDataValue == null)
      return NULL_SERIAL_VALUE;

    final _SupportedSerialization<VALUE, SERIAL> serialization = _getSerializationForType(pDataType);

    return new ISerialValue()
    {
      private final SERIAL serialValue = serialization.serialConverter.apply(pDataValue);

      @Override
      public void applyToStatement(PreparedStatement pStatement, int pIndex)
      {
        try
        {
          serialization.paramAppliance.applyArgument(pStatement, serialValue, pIndex);
        }
        catch (SQLException pE)
        {
          throw new OJDatabaseException("Unable to apply parameter to statement!", pE);
        }
      }
    };
  }

  /**
   * Retrieves a value from a {@link ResultSet} and converts it back to its original data value.
   *
   * @param pValueType the value type to retrieve
   * @param pResultSet the SQL result set to retrieve the value from
   * @param pIndex     the index to retrieve the result from
   * @return the original data value
   */
  @Nullable
  protected <VALUE> VALUE retrieveSerialValue(Class<VALUE> pValueType, ResultSet pResultSet, int pIndex)
  {
    final _SupportedSerialization<VALUE, ?> serialization = _getSerializationForType(pValueType);
    return serialization.retrieveConvertedResult(pResultSet, pIndex);
  }

  /**
   * Resolves a {@link _SupportedSerialization} for a specific data type.
   * Throws a {@link OJDatabaseException} if the data type is not supported.
   *
   * @param pDataType the data type to find the serialization for
   * @return the serialization for the data type
   */
  private <VALUE, SERIAL> _SupportedSerialization<VALUE, SERIAL> _getSerializationForType(Class<VALUE> pDataType)
  {
    if (!SUPPORTED_SERIALIZATIONS.containsKey(pDataType))
      throw new OJDatabaseException(pDataType.getName() + " is not a supported value type for serialization!");

    //noinspection unchecked
    return (_SupportedSerialization<VALUE, SERIAL>) SUPPORTED_SERIALIZATIONS.get(pDataType);
  }

  /**
   * Adds a supported data type that already is serializable for this serializer.
   * No converters are needed for these types.
   *
   * @param pType            the data type
   * @param pParamApplier    a function applying a serial value to a {@link PreparedStatement}
   * @param pResultRetriever a function retrieving a serial value from a {@link ResultSet}
   */
  private static <VALUE extends Serializable> void _put(Class<VALUE> pType, _ParamAppliance<VALUE> pParamApplier,
                                                        _ResultRetriever<VALUE> pResultRetriever)
  {
    _put(pType, identity(), pParamApplier, pResultRetriever, identity());
  }

  /**
   * Adds a supported data type for this serializer.
   *
   * @param pType            the data type
   * @param pSerialConverter a function converting a data value to its serial format
   * @param pParamApplier    a function applying a serial value to a {@link PreparedStatement}
   * @param pResultRetriever a function retrieving a serial value from a {@link ResultSet}
   * @param pDeserializer    a function the convert a serial value back to its data format
   */
  private static <VALUE, SERIAL extends Serializable> void _put(Class<VALUE> pType, Function<VALUE, SERIAL> pSerialConverter,
                                                                _ParamAppliance<SERIAL> pParamApplier,
                                                                _ResultRetriever<SERIAL> pResultRetriever,
                                                                Function<SERIAL, VALUE> pDeserializer)
  {
    SUPPORTED_SERIALIZATIONS.put(pType, new _SupportedSerialization<>(pSerialConverter, pParamApplier, pResultRetriever, pDeserializer));
  }

  /**
   * Defines a supported serialization for a specific data type.
   * Besides converting the data value to its persistent format and vice versa, this class is able to provide functions
   * that apply serial values to {@link PreparedStatement} or retrieve serial values from {@link ResultSet}.
   *
   * @param <VALUE>  the data type of values to serialize
   * @param <SERIAL> the serial value's type
   */
  private static class _SupportedSerialization<VALUE, SERIAL>
  {
    private final Function<VALUE, SERIAL> serialConverter;
    private final _ParamAppliance<SERIAL> paramAppliance;
    private final _ResultRetriever<SERIAL> resultRetriever;
    private final Function<SERIAL, VALUE> deserializer;

    /**
     * Initializes a supported serialization.
     *
     * @param pSerialConverter a function converting a data value to its serial format
     * @param pParamAppliance  a function applying a serial value to a {@link PreparedStatement}
     * @param pResultRetriever a function retrieving a serial value from a {@link ResultSet}
     * @param pDeserializer    a function the convert a serial value back to its data format
     */
    _SupportedSerialization(Function<VALUE, SERIAL> pSerialConverter, _ParamAppliance<SERIAL> pParamAppliance,
                            _ResultRetriever<SERIAL> pResultRetriever, Function<SERIAL, VALUE> pDeserializer)
    {
      serialConverter = pSerialConverter;
      paramAppliance = pParamAppliance;
      resultRetriever = pResultRetriever;
      deserializer = pDeserializer;
    }

    /**
     * Retrieves a serial value from a {@link ResultSet} and converts it back to its data type.
     *
     * @param pResultSet the result set to retrieve the serial value from
     * @param pIndex     the index of the serial value
     * @return the serial value in its data format
     */
    VALUE retrieveConvertedResult(ResultSet pResultSet, int pIndex)
    {
      try
      {
        final SERIAL serialValue = resultRetriever.retrieveResult(pResultSet, pIndex);

        if (serialValue == null)
          return null;

        return deserializer.apply(serialValue);
      }
      catch (SQLException pE)
      {
        throw new OJDatabaseException("Unable to retrieve result from SQL result set! index: " + pIndex, pE);
      }
    }
  }

  /**
   * Defines how to apply a serial value to a {@link PreparedStatement} at a certain index.
   * This is required to use the appropriate setter method for the correct data type.
   *
   * @param <SERIAL> the type of the serial value
   */
  @FunctionalInterface
  private interface _ParamAppliance<SERIAL>
  {
    /**
     * Applies a serial value to the parameter of a {@link PreparedStatement at a certain index.
     *
     * @param pStatement the statement to apply the value to
     * @param pValue     the serial value to apply
     * @param pIndex     the index to apply the value at
     */
    void applyArgument(PreparedStatement pStatement, SERIAL pValue, int pIndex) throws SQLException;
  }

  /**
   * Defines how to obtain a serial value from a {@link ResultSet} by index.
   * This is required to retrieve the value in their correct data types.
   *
   * @param <SERIAL> the type of the serial value
   */
  @FunctionalInterface
  private interface _ResultRetriever<SERIAL>
  {
    /**
     * Retrieves a serial value from a SQL result set by index.
     *
     * @param pResultSet the result set containing serial value
     * @param pIndex     the index to retrieve the value from
     * @return the serial value in it correct data type
     */
    SERIAL retrieveResult(ResultSet pResultSet, int pIndex) throws SQLException;
  }

  /**
   * Null serial value.
   */
  private static class _NullSerialValue implements ISerialValue
  {
    @Override
    public void applyToStatement(PreparedStatement pStatement, int pIndex)
    {
      try
      {
        pStatement.setObject(pIndex, null);
      }
      catch (SQLException pE)
      {
        throw new OJDatabaseException("Unable to apply parameter to statement!", pE);
      }
    }
  }
}
