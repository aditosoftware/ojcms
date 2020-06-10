package de.adito.ojcms.rest.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.bean.BeanSerializationException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON serialization provider for {@link IBean} instances. Only possible for beans that contain {@link ISerializableField} only.
 *
 * @author Simon Danner, 21.04.2020
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BeanSerializationProvider implements MessageBodyReader<IBean>, MessageBodyWriter<IBean>
{
  private static final Gson GSON = new Gson();

  @Override
  public boolean isReadable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public IBean readFrom(Class<IBean> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, String> pHttpHeaders, InputStream pEntityStream) throws WebApplicationException
  {
    try (InputStreamReader reader = new InputStreamReader(pEntityStream))
    {
      final Type mapType = new TypeToken<Map<String, Object>>()
      {
      }.getType();

      final Map<String, Serializable> serialBeanValues = GSON.fromJson(reader, mapType);

      try
      {
        final IBean bean = createEmptyBeanInstance(pType);
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
        throw new BeanSerializationException("Provide a default constructor for " + pType.getName() + "! May be private!", pE);
      }
    }
    catch (IOException pE)
    {
      throw new BeanSerializationException("Unable to read serialized bean!", pE);
    }
  }

  @Override
  public boolean isWriteable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public void writeTo(IBean pBean, Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, Object> pHttpHeaders, OutputStream pEntityStream) throws WebApplicationException
  {
    final Set<String> nonSerializableFields = pBean.streamFields() //
        .filter(pField -> !(pField instanceof ISerializableField)) //
        .map(IField::getName) //
        .collect(Collectors.toSet());

    if (!nonSerializableFields.isEmpty())
      throw new BeanSerializationException("Bean of type " + pType.getName() + " has a non serializable fields: " + nonSerializableFields);

    //noinspection unchecked
    final Map<String, Serializable> serialBeanValues = pBean.stream() //
        //Allow null values
        .collect(HashMap::new, (pMap, pTuple) -> pMap
            .put(pTuple.getField().getName(), ((ISerializableField) pTuple.getField()).toPersistent(pTuple.getValue())), Map::putAll);

    try (PrintWriter writer = new PrintWriter(pEntityStream))
    {
      writer.write(GSON.toJson(serialBeanValues));
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
