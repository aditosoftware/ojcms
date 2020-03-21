package de.adito.ojcms.sql.datasource.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.column.*;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.exceptions.BeanDataNotFoundException;
import de.adito.ojcms.utils.StringUtility;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.*;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition.isEqual;
import static java.util.function.Function.identity;

/**
 * Persistence model for persistent single beans.
 * All single beans are stored in a special database table.
 * Each row represents a bean by id and all its data converted to a JSON format.
 *
 * @author Simon Danner, 01.01.2020
 */
public class SingleBeanPersistenceModel implements IPersistenceModel
{
  //Several column definitions for the special single bean table
  public static final IColumnIdentification<String> ID_COLUMN = IColumnIdentification.of(BEAN_TABLE_BEAN_ID, String.class);
  private static final IColumnDefinition ID_COLUMN_DEFINITION =
      IColumnDefinition.of(BEAN_TABLE_BEAN_ID, EColumnType.STRING.create().primaryKey().modifiers(EColumnModifier.NOT_NULL));
  private static final IColumnIdentification<byte[]> CONTENT_COLUMN = IColumnIdentification.of(BEAN_TABLE_CONTENT, byte[].class);
  private static final IColumnDefinition CONTENT_COLUMN_DEFINITION = IColumnDefinition.of(BEAN_TABLE_CONTENT, EColumnType.BLOB.create());

  //Used for JSON serialization
  private static final Gson GSON = new GsonBuilder() //
      .serializeNulls() //
      .create();

  //Type token for bean data presented as Map<IField, Object>
  private static final Type CONTENT_TYPE_LITERAL = new TypeToken<Map<String, Object>>()
  {
  }.getType();

  private final String beanId;
  private final Map<String, IField<?>> fieldNameMapping;

  /**
   * Creates the database table for single beans if not created yet.
   *
   * @param pBuilder a builder to execute SQL statements
   */
  public static void createSingleBeanTableIfNecessary(OJSQLBuilder pBuilder)
  {
    pBuilder.ifTableNotExistingCreate(BEAN_TABLE_NAME, pCreate -> pCreate //
        .columns(ID_COLUMN_DEFINITION, CONTENT_COLUMN_DEFINITION) //
        .create());
  }

  /**
   * Initializes the single bean persistence model.
   *
   * @param pBeanId   the id of the single bean, identifies the row within the database table
   * @param pBeanType the bean type of the single bean
   */
  SingleBeanPersistenceModel(String pBeanId, Class<? extends IBean> pBeanType)
  {
    beanId = StringUtility.requireNotEmpty(pBeanId, "single bean id");
    fieldNameMapping = BeanReflector.reflectBeanFields(pBeanType).stream().collect(Collectors.toMap(IField::getName, identity()));
  }

  @Override
  public void initModelInDatabase(OJSQLBuilder pBuilder)
  {
    final boolean doesRowExist = pBuilder.doSelectOne(ID_COLUMN, pSelect -> pSelect //
        .from(BEAN_TABLE_NAME) //
        .where(isEqual(ID_COLUMN, beanId)) //
        .countRows() > 0);

    if (doesRowExist)
      return;

    pBuilder.doInsert(pInsert -> pInsert //
        .into(BEAN_TABLE_NAME) //
        .values(IColumnValueTuple.of(ID_COLUMN, beanId), _contentTuple(_createInitialContent())) //
        .insert());
  }

  /**
   * Loads single bean data by a {@link SingleBeanKey}.
   *
   * @param pKey     the key to identify the single bean
   * @param pBuilder a builder to execute SQL statements
   * @return the loaded persistent single bean data
   */
  public PersistentBeanData loadSingleBeanData(SingleBeanKey pKey, OJSQLBuilder pBuilder)
  {
    return pBuilder.doSelectOne(CONTENT_COLUMN, pSelect -> pSelect //
        .from(BEAN_TABLE_NAME) //
        .where(isEqual(ID_COLUMN, beanId)) //
        .firstResult() //
        .map(this::_fromPersistent) //
        .map(pBeanContent -> new PersistentBeanData(-1, pBeanContent))) //
        .orIfNotPresentThrow(() -> new BeanDataNotFoundException(pKey));
  }

  /**
   * Processes changes to the single bean values.
   *
   * @param pChangedValues the changed values mapped by bean fields
   * @param pBuilder       a builder to execute SQL statements
   */
  public void processChanges(Map<IField<?>, Object> pChangedValues, OJSQLBuilder pBuilder)
  {
    //This may be improved later to avoid redundant loading by query
    final PersistentBeanData changedData = loadSingleBeanData(new SingleBeanKey(beanId), pBuilder).integrateChanges(pChangedValues);

    pBuilder.doUpdate(pUpdate -> pUpdate //
        .table(BEAN_TABLE_NAME) //
        .set(_contentTuple(changedData.getData())) //
        .where(isEqual(ID_COLUMN, beanId)) //
        .update());
  }

  /**
   * Creates a {@link IColumnValueTuple} for the JSON representation of the values of the single bean.
   *
   * @param pBeanContent the content of the bean as map
   * @return the created column value tuple containing the bean data in its JSON format
   */
  private IColumnValueTuple<byte[]> _contentTuple(Map<IField<?>, Object> pBeanContent)
  {
    return IColumnValueTuple.of(CONTENT_COLUMN, _toPersistent(pBeanContent));
  }

  /**
   * Creates the initial content for the bean behind this persistence model.
   *
   * @return the initial bean data as map
   */
  private Map<IField<?>, Object> _createInitialContent()
  {
    return fieldNameMapping.values().stream() //
        //Allow null values
        .collect(HashMap::new, (pMap, pField) -> pMap.put(pField, pField.getInitialValue()), HashMap::putAll);
  }

  /**
   * Converts a map of bean data to its persistent JSON format as byte array.
   *
   * @param pBeanContent the content of a bean as map
   * @return the JSON format of the bean data as byte array
   */
  private static byte[] _toPersistent(Map<IField<?>, Object> pBeanContent)
  {
    final Map<String, Object> fieldNameValueMap = pBeanContent.entrySet().stream() //
        //Allow null values
        .collect(HashMap::new, (pMap, pEntry) -> pMap.put(pEntry.getKey().getName(), pEntry.getValue()), HashMap::putAll);

    return GSON.toJson(fieldNameValueMap).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Converts a byte array of JSON format for bean values back to a map.
   *
   * @param pSerialContent the serial JSON content as byte array
   * @return the bean data converted back to a map
   */
  private Map<IField<?>, Object> _fromPersistent(byte[] pSerialContent)
  {
    final String json = new String(pSerialContent, StandardCharsets.UTF_8);
    final Map<String, Object> fieldNameValueMap = GSON.fromJson(json, CONTENT_TYPE_LITERAL);

    return fieldNameValueMap.entrySet().stream() //
        //Allow null values
        .collect(HashMap::new, (pMap, pEntry) -> pMap
                .put(fieldNameMapping.get(pEntry.getKey()), _assureCorrectFormat(fieldNameMapping.get(pEntry.getKey()), pEntry.getValue())),
            HashMap::putAll);
  }

  /**
   * Assures that values that were converted back from a serial JSON format are in their correct data format.
   * This especially is relevant for number types.
   *
   * @param pField       the bean field the value is associated with
   * @param pSerialValue the serial value to check
   * @return the potentially adapted value
   */
  private Object _assureCorrectFormat(IField<?> pField, Object pSerialValue)
  {
    if (pSerialValue instanceof Number)
      //noinspection unchecked
      return _assureCorrectNumberFormat((IField<Number>) pField, (Number) pSerialValue);

    return pSerialValue;
  }

  /**
   * Assures that a {@link Number} that has been resolved from a persistent JSON format
   * fits to the data type defined by the associated bean field.
   *
   * @param pField        the bean field the number value is associated with
   * @param pSerialNumber the serial number instance
   * @return the potentially adapted number instance
   */
  private static <NUMBER extends Number> NUMBER _assureCorrectNumberFormat(IField<NUMBER> pField, Number pSerialNumber)
  {
    final Class<NUMBER> fieldType = pField.getDataType();

    if (pSerialNumber.getClass() == fieldType)
      //noinspection unchecked
      return (NUMBER) pSerialNumber;

    final Number correctFormat;

    if (fieldType == Integer.class)
      correctFormat = pSerialNumber.intValue();
    else if (fieldType == Double.class)
      correctFormat = pSerialNumber.doubleValue();
    else if (fieldType == Long.class)
      correctFormat = pSerialNumber.longValue();
    else if (fieldType == Float.class)
      correctFormat = pSerialNumber.floatValue();
    else if (fieldType == Short.class)
      correctFormat = pSerialNumber.shortValue();
    else
      throw new IllegalArgumentException("Unable to find correct number format for type: " + fieldType);

    //noinspection unchecked
    return (NUMBER) correctFormat;
  }
}
