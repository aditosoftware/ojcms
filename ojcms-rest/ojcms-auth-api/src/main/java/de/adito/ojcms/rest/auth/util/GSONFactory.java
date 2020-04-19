package de.adito.ojcms.rest.auth.util;

import com.google.gson.*;

import java.lang.reflect.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A GSON factory that provides a special {@link Gson} instance being able to de-/serialize interface types with only getters.
 * For the serialization of other types the default GSON mechanism will be used.
 *
 * @author Simon Danner, 09.12.2019
 */
public final class GSONFactory
{
  /**
   * Use this instance as global GSON de-/serializer.
   */
  public static final Gson GSON = new GsonBuilder() //
      .registerTypeHierarchyAdapter(Object.class, new _Serializer()) //
      .registerTypeHierarchyAdapter(Object.class, new _Deserializer()) //
      .create();

  private static final Gson DEFAULT_GSON = new Gson();

  private GSONFactory()
  {
  }

  /**
   * A serializer that serializes only-getter interfaces by their getter names and the associated value.
   * All other types will use the default GSON mechanism.
   */
  private static class _Serializer implements JsonSerializer<Object>
  {
    @Override
    public JsonElement serialize(Object pObject, Type pType, JsonSerializationContext pContext)
    {
      if (!(pType instanceof Class))
        return DEFAULT_GSON.toJsonTree(pObject, pType);

      final Class<?> objectType = (Class<?>) pType;

      if (!objectType.isInterface())
        return DEFAULT_GSON.toJsonTree(pObject, pType);

      final Function<Method, JsonElement> valueConverter = pMethod -> DEFAULT_GSON.toJsonTree(_invoke(pMethod, pObject));

      return Stream.of(objectType.getDeclaredMethods()) //
          .filter(pMethod -> pMethod.getReturnType() != Void.class) //
          .filter(pMethod -> pMethod.getParameterCount() == 0) //
          .reduce(new JsonObject(), (pJson, pMethod) ->
          {
            pJson.add(pMethod.getName(), valueConverter.apply(pMethod));
            return pJson;
          }, (j1, j2) -> j1);
    }

    /**
     * Invokes a certain method of an instance. Exceptions will be handled as runtime exception.
     *
     * @param pMethod   the method to invoke
     * @param pInstance the instance to invoke the method on
     * @return the method's result
     */
    private static Object _invoke(Method pMethod, Object pInstance)
    {
      try
      {
        return pMethod.invoke(pInstance);
      }
      catch (IllegalAccessException | InvocationTargetException pE)
      {
        throw new RuntimeException(pE);
      }
    }
  }

  /**
   * A deserializer that is able to deserialize only-getter interface types by creating proxies returning values by getter names from the
   * JSON element. All other types will use the default GSON mechanism.
   */
  private static class _Deserializer implements JsonDeserializer<Object>
  {
    @Override
    public Object deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException
    {
      if (!(pType instanceof Class))
        return DEFAULT_GSON.fromJson(pJson, pType);

      final Class<?> objectType = (Class<?>) pType;

      if (!objectType.isInterface())
        return DEFAULT_GSON.fromJson(pJson, pType);

      final JsonObject jsonObject = pJson.getAsJsonObject();

      return Proxy.newProxyInstance(GSONFactory.class.getClassLoader(), new Class<?>[]{objectType},
          (pObject, pMethod, pArgs) -> DEFAULT_GSON.fromJson(jsonObject.get(pMethod.getName()), pMethod.getReturnType()));
    }
  }
}
