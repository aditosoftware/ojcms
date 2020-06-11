package de.adito.ojcms.rest.auth.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.bean.BeanSerializationException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides a shared {@link Gson} instance that should be used by the REST server and the communicating clients. The {@link Gson}
 * instance includes a special de-/serialization mechanism for {@link IBean}.
 *
 * @author Simon Danner, 12.06.2020
 */
public final class OJGsonSerializer
{
  /**
   * Use this instance for server and client.
   */
  public static final Gson GSON_INSTANCE = new GsonBuilder() //
      .registerTypeHierarchyAdapter(IBean.class, new _Serializer()) //
      .registerTypeHierarchyAdapter(IBean.class, new _Deserializer()) //
      .create();

  private OJGsonSerializer()
  {
  }

  /**
   * The serializer for {@link IBean}. Converts a bean to a map of serialized values by field names.
   */
  private static class _Serializer implements JsonSerializer<IBean>
  {
    @Override
    public JsonElement serialize(IBean pBean, Type pType, JsonSerializationContext pSerializationContext)
    {
      final Set<String> nonSerializableFields = pBean.streamFields() //
          .filter(pField -> !(pField instanceof ISerializableField)) //
          .map(IField::getName) //
          .collect(Collectors.toSet());

      if (!nonSerializableFields.isEmpty())
        throw new BeanSerializationException(
            "Bean of type " + pType.getTypeName() + " has a non serializable fields: " + nonSerializableFields);

      //noinspection unchecked
      final Map<String, Serializable> serialBeanValues = pBean.stream() //
          //Allow null values
          .collect(HashMap::new, (pMap, pTuple) -> pMap
              .put(pTuple.getField().getName(), ((ISerializableField) pTuple.getField()).toPersistent(pTuple.getValue())), Map::putAll);

      return pSerializationContext.serialize(serialBeanValues);
    }
  }

  /**
   * The deserializer for {@link IBean}. Creates a new bean instance from a map of serialized values by field names.
   */
  private static class _Deserializer implements JsonDeserializer<IBean>
  {
    @Override
    public IBean deserialize(JsonElement pJsonElement, Type pType, JsonDeserializationContext pDeserializationContext)
        throws JsonParseException
    {
      final Type mapType = new TypeToken<Map<String, Object>>()
      {
      }.getType();

      final Map<String, Serializable> serialBeanValues = pDeserializationContext.deserialize(pJsonElement, mapType);

      try
      {
        //noinspection unchecked
        final IBean bean = createEmptyBeanInstance((Class<IBean>) pType);
        for (Map.Entry<String, Serializable> entry : serialBeanValues.entrySet())
        {
          final ISerializableField field = (ISerializableField) bean.getFieldByName(entry.getKey());
          //noinspection unchecked
          bean.setValue(field, field.fromPersistent(entry.getValue()));
        }

        return bean;
      }
      catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException pE)
      {
        throw new BeanSerializationException("Provide a default constructor for " + pType.getTypeName() + "! May be private!", pE);
      }
    }
  }

  /**
   * Creates an empty bean instance by calling its default constructor.
   *
   * @param pBeanType the bean type to instantiate
   * @return the create bean instance
   */
  private static IBean createEmptyBeanInstance(Class<IBean> pBeanType)
      throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException
  {
    final Constructor<IBean> defaultConstructor = pBeanType.getDeclaredConstructor();
    if (!defaultConstructor.isAccessible())
      defaultConstructor.setAccessible(true);

    return defaultConstructor.newInstance();
  }
}
