package de.adito.ojcms.utils.copy;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;
import de.adito.ojcms.utils.copy.exceptions.*;
import org.objenesis.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Static utility class providing mechanism to create deep or shallow copies.
 * But be aware: Most of these mechanism might be very resource intensive and not be suitable in performance critical environments.
 * And also be aware: It is not possible to create a deep copy of everything (for example interface based fields)
 *
 * There is NO 100% guarantee that the copy will be accurate.
 *
 * @author Simon Danner, 28.12.2018
 */
public final class SneakyCopyUtils
{
  private static final Objenesis SNEAKY_COPY_CREATOR = new ObjenesisStd();
  private static Gson gson;
  private static final Map<Class<?>, List<Field>> DECLARED_FIELDS_CACHE = new ConcurrentHashMap<>();

  private SneakyCopyUtils()
  {
  }

  /**
   * Creates a shallow copy of any value.
   *
   * @param pValueToCopy the value to copy
   * @param <VALUE>      the type of the value
   * @return the shallow copied value
   */
  @SuppressWarnings("unchecked")
  public static <VALUE> VALUE createShallowCopy(VALUE pValueToCopy)
  {
    if (pValueToCopy instanceof Collection)
    {
      try
      {
        return (VALUE) _createShallowCopyOfCollection((Collection<?>) pValueToCopy);
      }
      catch (CopyUnsupportedException pE)
      {
        //Then create the copy with the default mechanism
      }
    }

    final Class<VALUE> valueType = (Class<VALUE>) pValueToCopy.getClass();
    final VALUE instance = SNEAKY_COPY_CREATOR.newInstance(valueType);
    reflectDeclaredFields(valueType)
        .forEach(pField -> {
          try
          {
            if (!pField.isAccessible())
              pField.setAccessible(true);
            pField.set(instance, pField.get(pValueToCopy));
          }
          catch (IllegalAccessException pE)
          {
            throw new InternalCopyException(pE);
          }
        });
    return instance;
  }

  /**
   * Creates a deep copy of any value through {@link Gson}.
   * The correct expected data type may be important for generics or
   * if the runtime type of the value is private (e.g. SingletonList from {@link Collections}.
   *
   * @param pValueToCopy  the value to copy
   * @param pBaseType     the expected base data type of the value
   * @param pGenericTypes optional generic types of the base type
   * @param <VALUE>       the type of the value to copy
   * @return the deep copied value
   */
  public static <VALUE> VALUE createDeepCopy(VALUE pValueToCopy, Type pBaseType, Type... pGenericTypes) throws CopyUnsupportedException
  {
    if (gson == null)
      gson = new GsonBuilder()
          .registerTypeHierarchyAdapter(Class.class, _ClassTypeAdapter.INSTANCE)
          .create();
    try
    {
      final Type expectedType = pGenericTypes.length == 0 ? pBaseType : TypeToken.getParameterized(pBaseType, pGenericTypes).getType();
      return gson.fromJson(gson.toJson(pValueToCopy), expectedType);
    }
    catch (Exception pE)
    {
      throw new CopyUnsupportedException("Unable to create a deep copy of " + pValueToCopy, pE);
    }
  }

  /**
   * Reflects all declared fields of any type. This includes fields of possible super classes.
   * The declared fields will be cached for future requests
   *
   * @param pType the type to reflect the fields from
   * @return a list of all declared fields of the type
   */
  static List<Field> reflectDeclaredFields(Class<?> pType)
  {
    return DECLARED_FIELDS_CACHE.computeIfAbsent(pType, pTypeToReflect -> {
      final List<Field> declaredFields = new ArrayList<>();
      Class<?> current = pTypeToReflect;
      do
      {
        Stream.of(current.getDeclaredFields())
            .filter(pField -> !pField.isSynthetic())
            .forEach(pField -> {
              if (!pField.isAccessible())
                pField.setAccessible(true);
              declaredFields.add(pField);
            });
      }
      while ((current = current.getSuperclass()) != null);
      return declaredFields;
    });
  }

  /**
   * Creates a shallow copy of a collection.
   * This method expects an constructor taking another collection like {@link java.util.ArrayList#ArrayList(Collection)}.
   *
   * @param pCollectionToCopy the collection to create a shallow copy of
   * @param <ELEMENT>         the type of the element in the collection
   * @param <COLLECTION>      the runtime type of the collection to copy
   * @return the shallow copy of the collection
   * @throws CopyUnsupportedException if the required constructor is not present
   */
  private static <ELEMENT, COLLECTION extends Collection<ELEMENT>> COLLECTION _createShallowCopyOfCollection(COLLECTION pCollectionToCopy)
      throws CopyUnsupportedException
  {
    //noinspection unchecked
    final Class<COLLECTION> collectionType = (Class<COLLECTION>) pCollectionToCopy.getClass();
    try
    {
      return collectionType.getConstructor(Collection.class).newInstance(pCollectionToCopy);
    }
    catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException pE)
    {
      throw new CopyUnsupportedException("Unable to create a shallow copy of collection type " + collectionType.getName(), pE);
    }
  }

  /**
   * Allows {@link Class} instances to be copied through {@link Gson}.
   */
  private static class _ClassTypeAdapter extends TypeAdapter<Class<?>>
  {
    private static final TypeAdapter<Class<?>> INSTANCE = new _ClassTypeAdapter().nullSafe();

    @Override
    public void write(JsonWriter pOut, Class<?> pClassType) throws IOException
    {
      pOut.value(pClassType.getName());
    }

    @Override
    public Class<?> read(JsonReader pIn) throws IOException
    {
      try
      {
        return Class.forName(pIn.nextString());
      }
      catch (ClassNotFoundException pE)
      {
        throw new InternalCopyException(pE);
      }
    }
  }
}
